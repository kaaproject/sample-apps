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

const char savedConfig[] = "saved_config.cfg";

class UserConfigurationReceiver : public IConfigurationReceiver {
public:
    void displayConfiguration(const KaaRootConfiguration &configuration)
    {
        if (!configuration.AddressList.is_null()) {
            std::cout << "Configuration body:" << std::endl;
            auto links = configuration.AddressList.get_array();
            for (auto& e : links) {
                 std::cout << e.label << " - " << e.url << std::endl;
            }
        }
    }
    virtual void onConfigurationUpdated(const KaaRootConfiguration &configuration)
    {
        displayConfiguration(configuration);
    }
};

int main()
{
    std::cout << "Configuration demo started" << std::endl;
    std::cout << "--= Press Enter to exit =--" << std::endl;

    /*
     * Initialize the Kaa endpoint.
     */
    auto kaaClient = Kaa::newClient();

    /*
     * Set up a configuration subsystem.
     */
    IConfigurationStoragePtr storage(std::make_shared<FileConfigurationStorage>(savedConfig));
    kaaClient->setConfigurationStorage(storage);

    /*
     * Set configuration update receiver.
     */
    UserConfigurationReceiver receiver;
    kaaClient->addConfigurationListener(receiver);

    /*
     * Run the Kaa endpoint.
     */
    kaaClient->start();

    /*
     * Wait for the Enter key before exiting.
     */
    std::cin.get();

    /*
     * Stop the Kaa endpoint.
     */
    kaaClient->stop();

    std::cout << "Configuration demo stopped" << std::endl;

    return 0;
}
