package com.example.vikas.musify.events;

import com.example.vikas.musify.data.TagData;

public class TagAddedEvent {
    private TagData mTagData;

    public TagAddedEvent(TagData pTagData) {
        mTagData = pTagData;
    }

    public TagData getTag() {
        return mTagData;
    }
}
