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

#include <kaa/Kaa.hpp>
#include <kaa/IKaaClient.hpp>
#include <kaa/configuration/manager/IConfigurationReceiver.hpp>
#include <kaa/configuration/storage/FileConfigurationStorage.hpp>

using namespace kaa;

class UserConfigurationReceiver : public IConfigurationReceiver {
public:
    void displayConfiguration(const KaaRootConfiguration &configuration)
    {
        std::cout << "Sampling period is now " << configuration.samplePeriod << " seconds" << std::endl;
    }
    virtual void onConfigurationUpdated(const KaaRootConfiguration &configuration)
    {
        displayConfiguration(configuration);
    }
};

int main()
{
    /*
     * Initialize the Kaa endpoint.
     */
    auto kaaClient = Kaa::newClient();

    /*
     * Set up a configuration subsystem.
     */
    IConfigurationStoragePtr storage(std::make_shared<FileConfigurationStorage>("saved_config.cfg"));
    kaaClient->setConfigurationStorage(storage);

    /*
     * Display Endpoint Key Hash
     */
    std::cout << "Endpoint Key Hash: " << kaaClient->getEndpointKeyHash() << std::endl;

    /*
     * Set configuration update receiver.
     */
    UserConfigurationReceiver receiver;
    kaaClient->addConfigurationListener(receiver);

    /*
     * Run the Kaa endpoint.
     */
    kaaClient->start();

    std::cout << "Press Enter to stop" << std::endl;

    /*
     * Wait for the keypress.
     */
    std::cin.get();

    /*
     * Stop the Kaa endpoint.
     */
    kaaClient->stop();

    return 0;
}
