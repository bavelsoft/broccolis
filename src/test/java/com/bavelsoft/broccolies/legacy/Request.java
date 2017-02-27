package com.bavelsoft.broccolies.legacy;


public class Request {
    private Header _header;
    private String id;

    public Header getHeader() {
        return _header;
    }

    public void setHeader(Header header) {
        this._header = header;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSenderId(String senderId) {
        if (_header != null) {
            _header.setSenderId(senderId);
        }
    }

    public String getSenderId() {
        if (_header != null) {
            return _header.getSenderId();
        }
        return null;
    }
}
