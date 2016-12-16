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

using namespace kaa;

class FailoverStrategy: public DefaultFailoverStrategy
{
public:
    FailoverStrategy(IKaaClientContext &clientContext):
        DefaultFailoverStrategy(clientContext)
    {}

    ~FailoverStrategy() = default;

    FailoverStrategyDecision onFailover(KaaFailoverReason reason) override
    {
        switch (reason) {
            case KaaFailoverReason::ENDPOINT_NOT_REGISTERED:
                std::cerr << "Credentials are invalid: endpoint is not registered" << std::endl;
                return FailoverStrategyDecision(FailoverStrategyAction::STOP_CLIENT);
            case KaaFailoverReason::CREDENTIALS_REVOKED:
                std::cerr << "Credentials have been revoked" << std::endl;
                return FailoverStrategyDecision(FailoverStrategyAction::STOP_CLIENT);
            default:
                return FailoverStrategyDecision(FailoverStrategyAction::STOP_CLIENT);
        }
    }
};

class StateListener : public KaaClientStateListener
{
public:
    void onConnectionEstablished(const EndpointConnectionInfo& connection) override
    {
        if (connection.connectionAccepted_) {
            std::cout <<"Device state: REGISTERED" << std::endl;
        }
    }
};

int main()
{
    try {

        /*
         * Initialize the Kaa endpoint.
         */
        auto kaaClient = Kaa::newClient(std::make_shared<KaaClientPlatformContext>(), std::make_shared<StateListener>());

        /*
         * Set failover strategy
         */
        kaaClient->setFailoverStrategy(std::make_shared<FailoverStrategy>(kaaClient->getKaaClientContext()));

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

    } catch (KaaException &e) {
        std::cerr << e.what() << std::endl;
        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}
