/*
 * Copyright (C) 2016 mjrp1_000
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package pt.ua.ri.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mjrp1_000
 */
public class StaticUtils {
    private static final Logger logger = LoggerFactory.getLogger(StaticUtils.class);
    private static final long memLimit;
    private static final Runtime r;

    static {
        r = Runtime.getRuntime();
        memLimit = 4 * r.maxMemory() / 5;
    }

    public static boolean memoryLimitReached() {
        return estimatedUsedMemory() > memoryLimit();
    }

    public static boolean hasFreeMemory() {
        return estimatedUsedMemory() < r.maxMemory() / 4;
    }

    public static long estimatedUsedMemory() {
        return r.totalMemory() - r.freeMemory();
    }

    public static long memoryLimit() {
        return memLimit;
    }

    public static void forceGarbageCollection() {
        logger.debug("Garbage Collecting");
        r.gc();
        r.gc();
    }
}
