package com.jiwu.api.chat.common.cache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jiwu.api.chat.common.vo.ReactionVO;
import com.jiwu.api.common.main.dao.chat.ChatMessageReactionDAO;
import com.jiwu.api.common.main.pojo.chat.ChatMessageReaction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消息表情反应 Redis 缓存
 * <p>
 * 缓存结构（Hash）：
 * Key:   chat:reaction:msg:{msgId}
 * Field: {emojiType}        → 计数（字符串数字，支持 HINCRBY 原子操作）
 * Field: {emojiType}:users  → 逗号分隔的用户ID列表（按时间正序，截断前50）
 * TTL:   7天，写时续期
 * </p>
 *
 * @author Kiwi23333
 * @date 2026/02/17
 */
@Slf4j
@Component
public class ChatMessageReactionCache {

    private static final String KEY_PREFIX = "chat:reaction:msg:";
    private static final String USERS_SUFFIX = ":users";
    /**
     * 缓存有效期：7天
     */
    private static final long EXPIRE_SECONDS = 7 * 24 * 3600L;
    /**
     * 空标记有效期：5分钟（防缓存穿透）
     */
    private static final long EMPTY_EXPIRE_SECONDS = 5 * 60L;
    /**
     * 用户列表截断上限
     */
    private static final int MAX_USERS_IN_CACHE = 50;
    /**
     * 空标记 field
     */
    private static final String EMPTY_MARKER_FIELD = "__empty__";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ChatMessageReactionDAO chatMessageReactionDAO;

    private String buildKey(Long msgId) {
        return KEY_PREFIX + msgId;
    }

    // ===================== 读取操作 =====================

    /**
     * 批量获取消息的 reaction 聚合数据（不含 isCurrentUser）
     * <p>
     * 流程：先读缓存 → cache miss 的从 DB 加载并回填
     *
     * @param msgIds 消息ID集合
     * @return msgId → reaction 聚合列表
     */
    public Map<Long, List<ReactionVO>> batchGetReactions(Collection<Long> msgIds) {
        if (CollUtil.isEmpty(msgIds)) {
            return Collections.emptyMap();
        }
        List<Long> msgIdList = new ArrayList<>(msgIds);
        Map<Long, List<ReactionVO>> result = new HashMap<>();
        List<Long> missMsgIds = new ArrayList<>();

        // 1、Pipeline 批量读 Redis，减少 RTT（原为 N 次往返）
        List<Object> pipelineResults = stringRedisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                for (Long msgId : msgIdList) {
                    // StringRedisTemplate 下 K 为 String，buildKey 返回 String，强转以满足泛型
                    operations.opsForHash().entries((K) buildKey(msgId));
                }
                return null;
            }
        });

        for (int i = 0; i < msgIdList.size(); i++) {
            Long msgId = msgIdList.get(i);
            @SuppressWarnings("unchecked")
            Map<Object, Object> entries = (Map<Object, Object>) pipelineResults.get(i);
            if (entries == null || entries.isEmpty()) {
                missMsgIds.add(msgId);
            } else if (entries.containsKey(EMPTY_MARKER_FIELD)) {
                // 空标记，该消息无 reaction
                result.put(msgId, Collections.emptyList());
            } else {
                // 解析缓存数据
                result.put(msgId, parseHashToReactionVOList(entries));
            }
        }

        // 2、cache miss 的批量从 DB 加载
        if (CollUtil.isNotEmpty(missMsgIds)) {
            Map<Long, List<ReactionVO>> dbResult = loadFromDbAndFillCache(missMsgIds);
            result.putAll(dbResult);
        }

        return result;
    }

    /**
     * 获取单条消息的 reaction 聚合数据
     *
     * @param msgId 消息ID
     * @return reaction 聚合列表
     */
    public List<ReactionVO> getReactions(Long msgId) {
        Map<Long, List<ReactionVO>> map = batchGetReactions(Collections.singletonList(msgId));
        return map.getOrDefault(msgId, Collections.emptyList());
    }

    // ===================== 写入操作（toggle） =====================

    /**
     * 添加 reaction 后更新缓存
     *
     * @param msgId     消息ID
     * @param emojiType emoji编码
     * @param userId    用户ID
     */
    public void onReactionAdded(Long msgId, String emojiType, String userId) {
        String key = buildKey(msgId);
        try {
            // 移除空标记（如果存在）
            stringRedisTemplate.opsForHash().delete(key, EMPTY_MARKER_FIELD);
            // 计数 +1
            stringRedisTemplate.opsForHash().increment(key, emojiType, 1);
            // 更新用户列表
            appendUserToList(key, emojiType, userId);
            // 续期
            stringRedisTemplate.expire(key, EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("更新 reaction 缓存失败，将在下次查询时从 DB 重建。msgId={}, emoji={}", msgId, emojiType, e);
            // 缓存更新失败时删除该 key，下次查询自动重建
            stringRedisTemplate.delete(key);
        }
    }

    /**
     * 取消 reaction 后更新缓存
     *
     * @param msgId     消息ID
     * @param emojiType emoji编码
     * @param userId    用户ID
     */
    public void onReactionRemoved(Long msgId, String emojiType, String userId) {
        String key = buildKey(msgId);
        try {
            // 计数 -1
            Long newCount = stringRedisTemplate.opsForHash().increment(key, emojiType, -1);
            if (newCount != null && newCount <= 0) {
                // 该 emoji 已无人反应，删除计数和用户列表
                stringRedisTemplate.opsForHash().delete(key, emojiType, emojiType + USERS_SUFFIX);
            } else {
                // 从用户列表中移除
                removeUserFromList(key, emojiType, userId);
            }
            // 检查是否所有 emoji 都被清空
            Long hashSize = stringRedisTemplate.opsForHash().size(key);
            if (hashSize != null && hashSize == 0) {
                // 写入空标记
                stringRedisTemplate.opsForHash().put(key, EMPTY_MARKER_FIELD, "1");
                stringRedisTemplate.expire(key, EMPTY_EXPIRE_SECONDS, TimeUnit.SECONDS);
            } else {
                stringRedisTemplate.expire(key, EXPIRE_SECONDS, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("更新 reaction 缓存失败，将在下次查询时从 DB 重建。msgId={}, emoji={}", msgId, emojiType, e);
            stringRedisTemplate.delete(key);
        }
    }

    /**
     * 删除指定消息的 reaction 缓存
     *
     * @param msgId 消息ID
     */
    public void evict(Long msgId) {
        stringRedisTemplate.delete(buildKey(msgId));
    }

    // ===================== 内部方法 =====================

    /**
     * 从 DB 加载并回填缓存
     */
    private Map<Long, List<ReactionVO>> loadFromDbAndFillCache(List<Long> msgIds) {
        Map<Long, List<ReactionVO>> result = new HashMap<>();
        // 批量查询 DB
        List<ChatMessageReaction> reactions = chatMessageReactionDAO.listByMsgIds(msgIds);
        // 按 msgId 分组
        Map<Long, List<ChatMessageReaction>> grouped = reactions.stream()
                .collect(Collectors.groupingBy(ChatMessageReaction::getMsgId));

        for (Long msgId : msgIds) {
            List<ChatMessageReaction> msgReactions = grouped.get(msgId);
            String key = buildKey(msgId);
            if (CollUtil.isEmpty(msgReactions)) {
                // 无 reaction，写入空标记防穿透
                result.put(msgId, Collections.emptyList());
                stringRedisTemplate.opsForHash().put(key, EMPTY_MARKER_FIELD, "1");
                stringRedisTemplate.expire(key, EMPTY_EXPIRE_SECONDS, TimeUnit.SECONDS);
            } else {
                // 按 emojiType 分组聚合
                Map<String, List<ChatMessageReaction>> byEmoji = msgReactions.stream()
                        .collect(Collectors.groupingBy(ChatMessageReaction::getEmojiType,
                                LinkedHashMap::new, Collectors.toList()));
                List<ReactionVO> voList = new ArrayList<>();
                Map<String, String> hashEntries = new HashMap<>();
                for (Map.Entry<String, List<ChatMessageReaction>> entry : byEmoji.entrySet()) {
                    String emojiType = entry.getKey();
                    List<ChatMessageReaction> emojiReactions = entry.getValue();
                    int count = emojiReactions.size();
                    List<String> userIds = emojiReactions.stream()
                            .map(ChatMessageReaction::getUserId)
                            .collect(Collectors.toList());
                    List<String> truncatedUserIds = userIds.size() > MAX_USERS_IN_CACHE
                            ? userIds.subList(0, MAX_USERS_IN_CACHE) : userIds;

                    voList.add(ReactionVO.builder()
                            .emojiType(emojiType)
                            .count(count)
                            .userIds(truncatedUserIds)
                            .isCurrentUser(false) // 由调用方单独设置
                            .build());

                    // 回填缓存
                    hashEntries.put(emojiType, String.valueOf(count));
                    hashEntries.put(emojiType + USERS_SUFFIX, String.join(",", truncatedUserIds));
                }
                stringRedisTemplate.opsForHash().putAll(key, hashEntries);
                stringRedisTemplate.expire(key, EXPIRE_SECONDS, TimeUnit.SECONDS);
                result.put(msgId, voList);
            }
        }
        return result;
    }

    /**
     * 解析 Hash entries 为 ReactionVO 列表
     */
    private List<ReactionVO> parseHashToReactionVOList(Map<Object, Object> entries) {
        // 收集所有 emojiType（排除 :users 后缀和空标记）
        Set<String> emojiTypes = new LinkedHashSet<>();
        for (Object fieldObj : entries.keySet()) {
            String field = fieldObj.toString();
            if (field.equals(EMPTY_MARKER_FIELD)) {
                continue;
            }
            if (!field.endsWith(USERS_SUFFIX)) {
                emojiTypes.add(field);
            }
        }

        List<ReactionVO> result = new ArrayList<>();
        for (String emojiType : emojiTypes) {
            Object countObj = entries.get(emojiType);
            Object usersObj = entries.get(emojiType + USERS_SUFFIX);

            int count = 0;
            if (countObj != null) {
                try {
                    count = Integer.parseInt(countObj.toString());
                } catch (NumberFormatException e) {
                    log.warn("reaction 缓存计数解析失败: emojiType={}, value={}", emojiType, countObj);
                }
            }
            if (count <= 0) {
                continue;
            }

            List<String> userIds = Collections.emptyList();
            if (usersObj != null && StrUtil.isNotBlank(usersObj.toString())) {
                userIds = Arrays.asList(usersObj.toString().split(","));
            }

            result.add(ReactionVO.builder()
                    .emojiType(emojiType)
                    .count(count)
                    .userIds(userIds)
                    .isCurrentUser(false)
                    .build());
        }
        return result;
    }

    /**
     * 追加用户到列表（不超过上限）
     */
    private void appendUserToList(String key, String emojiType, String userId) {
        String usersField = emojiType + USERS_SUFFIX;
        Object usersObj = stringRedisTemplate.opsForHash().get(key, usersField);
        String usersStr = usersObj != null ? usersObj.toString() : "";

        if (StrUtil.isBlank(usersStr)) {
            stringRedisTemplate.opsForHash().put(key, usersField, userId);
        } else {
            String[] users = usersStr.split(",");
            if (users.length < MAX_USERS_IN_CACHE) {
                stringRedisTemplate.opsForHash().put(key, usersField, usersStr + "," + userId);
            }
            // 超过上限不追加，count 仍然准确
        }
    }

    /**
     * 从列表中移除用户
     */
    private void removeUserFromList(String key, String emojiType, String userId) {
        String usersField = emojiType + USERS_SUFFIX;
        Object usersObj = stringRedisTemplate.opsForHash().get(key, usersField);
        if (usersObj == null) {
            return;
        }
        String usersStr = usersObj.toString();
        List<String> users = new ArrayList<>(Arrays.asList(usersStr.split(",")));
        users.remove(userId);
        if (users.isEmpty()) {
            stringRedisTemplate.opsForHash().delete(key, usersField);
        } else {
            stringRedisTemplate.opsForHash().put(key, usersField, String.join(",", users));
        }
    }
}
