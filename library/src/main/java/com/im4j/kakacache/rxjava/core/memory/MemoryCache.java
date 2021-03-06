package com.im4j.kakacache.rxjava.core.memory;

import com.im4j.kakacache.rxjava.common.utils.Utils;
import com.im4j.kakacache.rxjava.core.BasicCache;
import com.im4j.kakacache.rxjava.core.CacheEntry;
import com.im4j.kakacache.rxjava.core.CacheTarget;
import com.im4j.kakacache.rxjava.core.memory.journal.IMemoryJournal;
import com.im4j.kakacache.rxjava.core.memory.storage.IMemoryStorage;

import java.util.Collection;

/**
 * 内存缓存
 * @version 0.1 king 2016-04
 */
public final class MemoryCache extends BasicCache {

    private final IMemoryStorage mStorage;
    private final IMemoryJournal mJournal;

    public MemoryCache(IMemoryStorage storage,
                       IMemoryJournal journal,
                       long maxSize,
                       long maxQuantity) {
        super(maxSize, maxQuantity);
        this.mStorage = storage;
        this.mJournal = journal;
    }


    @Override
    protected <T> T doLoad(String key) {
        return (T) mStorage.load(key);
    }

    @Override
    protected <T> boolean doSave(String key, T value, int maxAge, CacheTarget target) {
        if (target == null || target == CacheTarget.NONE || target == CacheTarget.Disk) {
            return true;
        }

        // 写入缓存
        if (mStorage.save(key, value)) {
            mJournal.put(key, new CacheEntry(key, maxAge, target));
            return true;
        }
        return false;
    }

    @Override
    protected boolean isExpiry(String key) {
        CacheEntry entry = mJournal.get(key);
        return entry == null || entry.isExpiry();
    }

    @Override
    protected boolean doContainsKey(String key) {
        return mJournal.containsKey(key);
    }

    /**
     * 删除缓存
     * @param key
     */
    @Override
    protected boolean doRemove(String key) {
        return mStorage.remove(key) && mJournal.remove(key);
    }

    /**
     * 清空缓存
     */
    @Override
    protected boolean doClear() {
        return mStorage.clear() && mJournal.clear();
    }

    @Override
    public Collection<CacheEntry> snapshot() {
        return mJournal.snapshot();
    }

    @Override
    public String getLoseKey() {
        return mJournal.getLoseKey();
    }

    @Override
    public long getTotalSize() {
        long size = mStorage.getTotalSize();
        Utils.checkNotLessThanZero(size);
        return size;
    }

    @Override
    public long getTotalQuantity() {
        long quantity = mStorage.getTotalQuantity();
        Utils.checkNotLessThanZero(quantity);
        return quantity;
    }

}
