package com.fotonauts.fwissr;

import java.io.IOException;

@SuppressWarnings("serial")
public class FwissrRuntimeException extends RuntimeException {

    public FwissrRuntimeException(String string) {
        super(string);
    }

    public FwissrRuntimeException(String string, IOException e) {
        super(string, e);
    }

}
