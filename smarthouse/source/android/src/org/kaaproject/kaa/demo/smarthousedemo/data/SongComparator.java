/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.demo.smarthousedemo.data;

import java.util.Comparator;

import org.kaaproject.kaa.demo.smarthouse.music.SongInfo;

public class SongComparator implements Comparator<SongInfo> {

    @Override
    public int compare(SongInfo lhs, SongInfo rhs) {
        return lhs.getTitle().toUpperCase().compareTo(rhs.getTitle().toUpperCase());
    }

}
