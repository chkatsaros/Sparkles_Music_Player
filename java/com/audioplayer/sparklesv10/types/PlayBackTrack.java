package com.audioplayer.sparklesv10.types;

import com.audioplayer.sparklesv10.utility.SparklesUtil;

public class PlayBackTrack {

    public long mId;
    public long sourceId;
    public SparklesUtil.IdType mIdType;
    public int mCurrentPosition;

    public PlayBackTrack(long mId, long sourceId, SparklesUtil.IdType mIdType, int mCurrentPosition) {
        this.mId = mId;
        this.sourceId = sourceId;
        this.mIdType = mIdType;
        this.mCurrentPosition = mCurrentPosition;
    }


}
