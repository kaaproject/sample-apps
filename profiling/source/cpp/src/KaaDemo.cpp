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

#include <kaa/Kaa.hpp>
#include <kaa/IKaaClient.hpp>
#include <kaa/failover/IFailoverStrategy.hpp>
#include <kaa/failover/DefaultFailoverStrategy.hpp>
#include <kaa/profile/IProfileContainer.hpp>
#include <kaa/profile/DefaultProfileContainer.hpp>
#include <kaa/configuration/manager/IConfigurationReceiver.hpp>

#include <boost/filesystem.hpp>

#include <vector>

using namespace kaa;

class ProfileData
{
public:
    ProfileData(bool audioSupport, bool videoSupport,
            bool vibroSupport)
    {
        profile_.audioSupport = audioSupport;
        profile_.videoSupport = videoSupport;
        profile_.vibroSupport = vibroSupport;
    }

    ~ProfileData() = default;

    const KaaProfile &getProfile() const
    {
        return profile_;
    }

private:
    KaaProfile profile_;
};

static const std::vector<ProfileData> clientProfiles = {
    ProfileData(false, false, true),
    ProfileData(true, false, true),
    ProfileData(true, true, true),
};

using IKaaClientPtr = std::shared_ptr<IKaaClient>;

static void printConfiguration(const KaaRootConfiguration &config)
{
    std::cout << std::boolalpha;
    std::cout << "Audio Support: " << config.audioSubscriptionActive << std::endl;
    std::cout << "Video Support: " << config.videoSubscriptionActive << std::endl;
    std::cout << "Vibro Support: " << config.vibroSubscriptionActive << std::endl;
    std::cout << std::noboolalpha;
}

class ConfigurationListener: public IConfigurationReceiver
{
public:
    ConfigurationListener(IKaaClientPtr kaaClient):
        kaaClient_(kaaClient)
    {
        if (!kaaClient_) {
            throw std::invalid_argument("KaaClient is null");
        }
    }

    ~ConfigurationListener() = default;

    virtual void onConfigurationUpdated(const KaaRootConfiguration &configuration)
    {
        std::cout << "Configuration updated!" << std::endl;
        std::cout << "Endpoint ID: " << kaaClient_->getEndpointKeyHash() << std::endl;
        printConfiguration(configuration);
        std::cout << std::endl;
    }

private:
    IKaaClientPtr kaaClient_;
};

typedef std::shared_ptr<ConfigurationListener> ConfigurationListenerPtr;

class KaaClientManager
{
public:
    KaaClientManager() = default;

    ~KaaClientManager()
    {
        for (auto &client : kaaClients_) {
            client->stop();
        }
    }

    bool spawnKaaClient(const KaaProfile &profile)
    {
        std::string clientDir = "client" + std::to_string(kaaClients_.size());

        boost::filesystem::path dir(clientDir);

        if (!boost::filesystem::exists(dir) && !boost::filesystem::create_directory(dir)) {
            std::cerr << "Failed to create directory " << dir.c_str() << std::endl;
            return false;
        }

        IKaaClientPlatformContextPtr clientContext
            = std::make_shared<KaaClientPlatformContext>();

        auto &clientProperties = clientContext->getProperties();
        clientProperties.setWorkingDirectoryPath(clientDir);

        auto kaaClient = Kaa::newClient(clientContext);
        kaaClients_.push_back(kaaClient);

        kaaClient->start();

        auto configurationListener =
            std::make_shared<ConfigurationListener>(ConfigurationListener(kaaClient));
        kaaClient->addConfigurationListener(*configurationListener);
        configurationListeners_.push_back(configurationListener);


        auto profileContainer =
            std::make_shared<DefaultProfileContainer>(DefaultProfileContainer(profile));
        kaaClient->setProfileContainer(profileContainer);
        return true;
    }

    void updateProfiles()
    {
        for ( auto client : kaaClients_)
        {
            client->updateProfile();
        }
    }

private:
    std::vector<IKaaClientPtr> kaaClients_;
    std::vector<ConfigurationListenerPtr> configurationListeners_;
};

int main()
{
    KaaClientManager clientManager;

    for (const auto &profile : clientProfiles) {
        if (!clientManager.spawnKaaClient(profile.getProfile())) {
            std::cerr << "Failed to start Kaa client" << std::endl;
            return EXIT_FAILURE;
        }
    }

    clientManager.updateProfiles();

    std::cout << "Spawned " << clientProfiles.size() << " clients" << std::endl;

    std::cout << "Press any key to exit" << std::endl;

    std::cin.get();

    return EXIT_SUCCESS;
}
