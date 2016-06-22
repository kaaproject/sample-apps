/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.cellmonitor.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for project constants
 */
public class CellMonitorConstants {

    public static final Logger LOG = LoggerFactory.getLogger(CellMonitorConstants.class);

    public static final int CELL_LOCATION_CHANGED = 0;
    public static final int GPS_LOCATION_CHANGED = 1;
    public static final int SIGNAL_STRENGTH_CHANGED = 2;
    public static final int LOG_SENT = 3;

    public static final int UNDEFINED = -1;

}
