/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

#define HIGH 1
#define LOW 0
#define INPUT 1
#define OUTPUT 0

#define LED_1 121
#define LED_2 122

bool digitalPinMode(int pin, int dir)
{
    FILE *fd = nullptr;
    char fName[128];

    //Exporting the pin to be used
    if(( fd = fopen("/sys/class/gpio/export", "w")) == NULL) {
        printf("Error: unable to export pin\n");
        return false;
    }

    fprintf(fd, "%d\n", pin);
    fclose(fd);

    // Setting direction of the pin
    sprintf(fName, "/sys/class/gpio/gpio%d/direction", pin);
    if ((fd = fopen(fName, "w")) == NULL) {
        printf("Error: can't open pin direction\n");
        return false;
    }

    if (dir == OUTPUT) {
        fprintf(fd, "out\n");
    } else {
        fprintf(fd, "in\n");
    }

    fclose(fd);
    return true;
}


int digitalRead(int pin)
{
    FILE *fd = nullptr;
    char fName[128];
    char val[2];

    //Open pin value file
    sprintf(fName, "/sys/class/gpio/gpio%d/value", pin);
    if ((fd = fopen(fName, "r")) == NULL) {
        printf("Error: can't open pin value\n");
        return false;
    }

    fgets(val, 2, fd);
    fclose(fd);

    return atoi(val);
}


bool digitalWrite(int pin, int val)
{
    FILE *fd = nullptr;
    char fName[128];

    // Open pin value file
    sprintf(fName, "/sys/class/gpio/gpio%d/value", pin);
    if ((fd = fopen(fName, "w")) == NULL) {
        printf("Error: can't open pin value\n");
        return false;
    }

    if (val == HIGH) {
        fprintf(fd, "1\n");
    } else {
        fprintf(fd, "0\n");
    }

    fclose(fd);
    return true;
}

class ECFListener: public RemoteControlECF::RemoteControlECFListener
{
public:
    ECFListener(RemoteControlECF &rm): remote(rm)
    {
        nsRemoteControlECF::GpioStatus status;
        status.id = LED_1;
        status.status = false;
        leds.push_back(status);
        status.id = LED_2;
        leds.push_back(status);
    }

    void onEvent(const nsRemoteControlECF::DeviceInfoRequest& event, const std::string& source)
    {
        nsRemoteControlECF::DeviceInfoResponse response;

        response.deviceName = "artik";
        response.model      = "model_5";
        response.gpioStatus = leds;

        remote.sendEvent(response);
    }

    void onEvent(const nsRemoteControlECF::GpioToggleRequest& event, const std::string& source)
    {
        if (event.gpio.status) {
            digitalWrite(event.gpio.id, 1);
            setLedStatus(event.gpio.id, true);
        } else {
            digitalWrite(event.gpio.id, 0);
            setLedStatus(event.gpio.id, false);
        }
    }

protected:
    RemoteControlECF &remote;
    std::vector<nsRemoteControlECF::GpioStatus> leds;

private:
    void setLedStatus(int id, bool status)
    {
        for (std::size_t i = 0; i < leds.size(); ++i)
            if (leds[i].id == id) {
                leds[i].status = status;
                return;
            }
    }
};

int main()
{
    digitalPinMode(LED_1, OUTPUT);
    digitalPinMode(LED_2, OUTPUT);

    digitalWrite(LED_1, 0);
    digitalWrite(LED_2, 0);

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
