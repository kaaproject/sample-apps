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

package org.kaaproject.kaa.demo.verifiersdemo.entity;

import org.kaaproject.kaa.demo.verifiersdemo.UserVerifierApp;

public final class User {

    private final String mId;
    private final String mName;
    private final String mToken;
    private final UserVerifierApp.AccountType mType;

    private String mCurrentInfo;
    private String mEventMessagesText;

    public User(UserVerifierApp.AccountType type, String id, String name, String token) {
        this.mName = name;
        this.mId = id;
        this.mToken = token;
        this.mType = type;
    }

    public void setCurrentInfo(String currentInfo) {
        this.mCurrentInfo = currentInfo;
    }

    public String getEventMessagesText() {
        return mEventMessagesText;
    }

    public void setEventMessagesText(String eventMessagesText) {
        this.mEventMessagesText = eventMessagesText;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getToken() {
        return mToken;
    }

    public String getCurrentInfo() {
        return mCurrentInfo;
    }

    public UserVerifierApp.AccountType getType() {
        return mType;
    }


}
