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


#include <memory>
#include <thread>
#include <cstdint>
#include <cstdio>
#include <iostream>

#include <kaa/Kaa.hpp>
#include <kaa/event/registration/IUserAttachCallback.hpp>
#include <kaa/event/IFetchEventListeners.hpp>
#include <kaa/event/gen/EventFamilyFactory.hpp>
#include <kaa/event/gen/RemoteControlECF.hpp>

using namespace kaa;

#define NUM_GPIO_LEDS 4

class ECFListener: public RemoteControlECF::RemoteControlECFListener
{
public:
    ECFListener(RemoteControlECF &rm): remote(rm)
    {
        nsRemoteControlECF::GpioStatus status;
        
        status.status = false;
        for (int n = 0; n < NUM_GPIO_LEDS; n++) {
            status.id = n;
            leds.push_back(status);
        }
    }

    void onEvent(const nsRemoteControlECF::DeviceInfoRequest& event, const std::string& source)
    {
        nsRemoteControlECF::DeviceInfoResponse response;

        response.deviceName = "posix";
        response.model      = "01";
        response.gpioStatus = leds;

        remote.sendEvent(response);
    }

    void onEvent(const nsRemoteControlECF::GpioToggleRequest& event, const std::string& source)
    {
        if (event.gpio.status) {
            setLedStatus(event.gpio.id, true);
        } else {
            setLedStatus(event.gpio.id, false);
        }
        
        printLedStatus();
    }

protected:
    RemoteControlECF &remote;
    std::vector<nsRemoteControlECF::GpioStatus> leds;

private:
    void setLedStatus(int id, bool status)
    {
        for (std::size_t i = 0; i < leds.size(); ++i) {
            if (leds[i].id == id) {
                leds[i].status = status;
                return;
            }
        }
    }
    
    void printLedStatus(void)
    {
        printf("GPIO LED status: ");
        
        for (std::size_t i = 0; i < leds.size(); ++i) {
            printf("%d", (int)leds[i].status);
        }
        
        printf("\n");
    }
};

int main()
{
    /*
     * Initialize the Kaa endpoint.
     */
    auto kaaClient =  Kaa::newClient();

    ECFListener ecfListener(kaaClient->getEventFamilyFactory().getRemoteControlECF());
    kaaClient->getEventFamilyFactory().getRemoteControlECF().addEventFamilyListener(ecfListener);
    kaaClient->setEndpointAccessToken(DEMO_ACCESS_TOKEN);

    /*
     * Run the Kaa endpoint.
     */
    kaaClient->start();


    while (true) {
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    /*
     * Stop the Kaa endpoint.
     */
    kaaClient->stop();

    return 0;
}
