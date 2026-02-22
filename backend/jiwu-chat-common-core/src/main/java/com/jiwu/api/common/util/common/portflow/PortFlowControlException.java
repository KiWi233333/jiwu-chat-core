package com.jiwu.api.common.util.common.portflow;

import com.jiwu.api.common.enums.ResultStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义限流异常
 *
 * @author linzhihan
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PortFlowControlException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    protected Integer errorCode;

    /**
     * 错误信息
     */
    protected String errorMsg;

    public PortFlowControlException() {
        super();
    }

    public PortFlowControlException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public PortFlowControlException(ResultStatus error) {
        super(error.getMessage());
        this.errorCode = error.getCode();
        this.errorMsg = error.getMessage();
    }


}
