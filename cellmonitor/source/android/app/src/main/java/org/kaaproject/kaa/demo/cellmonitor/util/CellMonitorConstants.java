package org.kaaproject.kaa.demo.cellmonitor.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class CellMonitorConstants {

    public static final Logger LOG = LoggerFactory.getLogger(CellMonitorConstants.class);

    public static final int CELL_LOCATION_CHANGED = 0;
    public static final int GPS_LOCATION_CHANGED = 1;
    public static final int SIGNAL_STRENGTH_CHANGED = 2;
    public static final int LOG_SENT = 3;

    public static final int UNDEFINED = -1;

}
