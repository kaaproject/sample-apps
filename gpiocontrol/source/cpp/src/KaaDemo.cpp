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
            _setLedStatus(event.gpio.id, true);
        } else {
            _setLedStatus(event.gpio.id, false);
        }
        
        _printLedStatus();
    }

protected:
    RemoteControlECF &remote;
    std::vector<nsRemoteControlECF::GpioStatus> leds;

private:
    void _setLedStatus(int id, bool status)
    {
        for (auto &led : leds) {
            if (led.id == id) {
                led.status = status;
                return;
            }
        }
    }
    
    void _printLedStatus()
    {
        std::cout << "GPIO LED status: ";
        
        for (auto &led : leds) {
            std::cout << led.status;
        }
        
        std::cout << std::endl;
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

    std::cout << "GPIO demo started" << std::endl;

    while (true) {
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    /*
     * Stop the Kaa endpoint.
     */
    kaaClient->stop();

    return EXIT_SUCCESS;
}
