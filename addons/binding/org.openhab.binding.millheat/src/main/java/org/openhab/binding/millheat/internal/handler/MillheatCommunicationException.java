package org.openhab.binding.millheat.internal.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.millheat.internal.dto.AbstractRequest;
import org.openhab.binding.millheat.internal.dto.AbstractResponse;

public class MillheatCommunicationException extends Exception {

    private int errorCode = 0;

    public MillheatCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MillheatCommunicationException(String message) {
        super(message);
    }

    public MillheatCommunicationException(@NonNull AbstractRequest req, @NonNull AbstractResponse rsp) {
        super("Server responded with error to request " + req.getClass().getSimpleName() + "/" + req.getRequestUrl()
                + ": " + rsp.errorCode + "/" + rsp.errorName + "/" + rsp.errorDescription);
        this.errorCode = rsp.errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
