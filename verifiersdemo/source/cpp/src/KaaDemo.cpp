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
#include <iostream>

#include <kaa/Kaa.hpp>
#include <kaa/logging/LoggingUtils.hpp>
#include <kaa/event/registration/IUserAttachCallback.hpp>
#include <kaa/event/IFetchEventListeners.hpp>
#include <kaa/event/gen/EventFamilyFactory.hpp>
#include <kaa/event/gen/VerifiersDemoEventClassFamily.hpp>

using namespace kaa;

class VerifiersDemoEventClassFamilyListener : public VerifiersDemoEventClassFamily::VerifiersDemoEventClassFamilyListener {
public:

    VerifiersDemoEventClassFamilyListener(EventFamilyFactory &factory) : eventFactory_(factory) { }
    void onEvent(const nsVerifiersDemoEventClassFamily::MessageEvent& event, const std::string& source) override
    {
        (void)source;
        std::cout << "Message was received!" << std::endl;

        if(!event.message.is_null()) {
            std::cout << "Message: " << event.message.get_string() << std::endl;
        }
    }

private:
    EventFamilyFactory& eventFactory_;
};

int main()
{
    std::cout << "Verifiers demo started" << std::endl;
    /*
     * Initialize the Kaa endpoint.
     */
    auto kaaClient = Kaa::newClient();
    
    kaaClient->setEndpointAccessToken(DEMO_ACCESS_TOKEN);

    std::cout << "Endpoint access token: " << DEMO_ACCESS_TOKEN << std::endl;

    /*
     * Run the Kaa endpoint.
     */

    kaaClient->start();

    VerifiersDemoEventClassFamilyListener messageListener(kaaClient->getEventFamilyFactory());

    kaaClient->getEventFamilyFactory().getVerifiersDemoEventClassFamily().addEventFamilyListener(messageListener);

    std::cout << "Press Enter to stop" << std::endl;

    /*
     * Wait for the keypress.
     */
    std::cin.get();

    kaaClient->stop();

    std::cout << "Verifiers demo stopped" << std::endl;

    return 0;
}
