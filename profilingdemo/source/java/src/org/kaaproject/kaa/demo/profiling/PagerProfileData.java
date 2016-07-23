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

package org.kaaproject.kaa.demo.profiling;

import java.util.ArrayList;

/**
 * Class for storing test data and showing all functionality
 *
 * @author Maksym Liashenko
 */
public class PagerProfileData {

    private static ArrayList<InnerData> profiles = new ArrayList<>();

    public static ArrayList<InnerData> init() {
        if (!profiles.isEmpty()) {
            return profiles;
        }

        profiles.add(new InnerData(true, true, true));
        profiles.add(new InnerData(false, true, true));
        profiles.add(new InnerData(true, true, false));
        profiles.add(new InnerData(true, false, true));
        profiles.add(new InnerData(true, false, false));
        profiles.add(new InnerData(false, true, false));
        profiles.add(new InnerData(false, false, true));
        profiles.add(new InnerData(false, false, false));

        return profiles;
    }

    static class InnerData {
        private boolean isVibro;
        private boolean isAudio;
        private boolean isVideo;

        public InnerData(boolean isAudio, boolean isVibro, boolean isVideo) {
            this.isVideo = isVideo;
            this.isAudio = isAudio;
            this.isVibro = isVibro;
        }

        public boolean isVibro() {
            return isVibro;
        }

        public boolean isAudio() {
            return isAudio;
        }

        public boolean isVideo() {
            return isVideo;
        }
    }
}
