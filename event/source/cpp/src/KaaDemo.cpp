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
#include <string>
#include <cstdint>
#include <iostream>

#include <kaa/Kaa.hpp>
#include <kaa/event/registration/IUserAttachCallback.hpp>
#include <kaa/event/IFetchEventListeners.hpp>
#include <kaa/event/gen/EventFamilyFactory.hpp>
#include <kaa/event/gen/Chat.hpp>

using namespace kaa;

static const char * const CHAT_EVENT_FQN = "org.kaaproject.kaa.examples.event.ChatEvent";
static const char * const CHAT_MESSAGE_FQN = "org.kaaproject.kaa.examples.event.Message";

class ChatApp: public Chat::ChatListener {
public:
    ChatApp(EventFamilyFactory &factory):
        eventFactory_(factory)
    {}

    ~ChatApp() = default;

    bool joinRoom(const std::string &name)
    {
        if (std::find(rooms_.begin(), rooms_.end(), name) == rooms_.end()) {
            return false;
        }
        currentRoom_ = name;
        return true;
    }

    void leaveRoom() {
        currentRoom_ = "";
    }

    bool createRoom(const std::string &name)
    {
        if (std::find(rooms_.begin(), rooms_.end(), name) == rooms_.end()) {
            nsChat::ChatEvent cev;
            cev.ChatName = name;
            cev.EventType = nsChat::CREATE;
            eventFactory_.getChat().sendEventToAll(cev);
            rooms_.push_back(name);
            return true;
        }
        return false;
    }

    bool deleteRoom(const std::string &name)
    {
        auto pos = std::find(rooms_.begin(), rooms_.end(), name);
        if (pos != rooms_.end()) {
            nsChat::ChatEvent cev;
            cev.ChatName = name;
            cev.EventType = nsChat::DELETE;
            eventFactory_.getChat().sendEventToAll(cev);
            rooms_.erase(pos);
            return true;
        }
        return false;
    }

    const std::vector<std::string> &getRooms() const
    {
        return rooms_;
    }

    void sendMessage(const std::string message)
    {
        if (currentRoom_.empty()) {
            return;
        }

        nsChat::Message msg;
        msg.Message = message;
        msg.ChatName = currentRoom_;
        eventFactory_.getChat().sendEventToAll(msg);
    }

    virtual void onEvent(const nsChat::ChatEvent &event, const std::string &source)
    {
        static_cast<void>(source);

        switch (event.EventType) {
            case nsChat::CREATE:
                createRoom(event.ChatName);
            break;
            case nsChat::DELETE:
                deleteRoom(event.ChatName);
            break;
        }
    }

    virtual void onEvent(const nsChat::Message &message, const std::string &source)
    {
        static_cast<void>(source);
        if (message.ChatName == currentRoom_) {
            std::cout << message.Message << std::endl;
        }
    }

private:
    EventFamilyFactory &eventFactory_;
    std::string currentRoom_;
    std::vector<std::string> rooms_;
};

class ChatMenu {
public:
    ChatMenu(ChatApp &chat):
        chat_(chat)
    {}

    ~ChatMenu() = default;

    bool process(const std::string &input)
    {
        if (inMenu_) {
            return processCommand(input);
        }

        if (input == "/quit") {
            inMenu_ = true;
            chat_.leaveRoom();
            printHelp();
            printRooms();
        } else {
            chat_.sendMessage(input);
        }
        return true;
    }

    void printHelp() const {
        std::cout << "Available commands:\n";
        std::cout << "1. Join room\n";
        std::cout << "2. Create room\n";
        std::cout << "3. Delete room\n";
        std::cout << "4. List available rooms\n";
        std::cout << "5. Exit application\n";
    }

    void printRooms() const {
        const auto &rooms = chat_.getRooms();
        if (rooms.size() == 0) {
            std::cout << "No rooms available\n";
            return;
        }
        std::cout << "Available rooms:\n";
        for (const auto &room : rooms) {
            std::cout << room << '\n';
        }
    }

private:
    bool processCommand(const std::string &command) {
        if (command.compare("1") == 0) {
            return command_join();
        } else if (command.compare("2") == 0) {
            return command_create();
        }  else if (command.compare("3") == 0) {
            return command_delete();
        } else if (command.compare("4") == 0) {
            printRooms();
            return true;
        } else if (command.compare("5") == 0) {
            return false;
        }

        std::cout << "Unknown command " << command << '\n';
        return true;
    }

    bool command_join()
    {
        std::cout << "Enter chat room name:" << std::endl;
        std::string room;
        std::getline(std::cin, room);

        if (room.empty()) {
            std::cout << "Wrong command syntax\n";
            printHelp();
            return true;
        }
        if (!chat_.joinRoom(room)) {
            std::cout << "Failed to join " << room << '\n';
            printRooms();
            return true;
        }
        std::cout << "Joined " << room << '\n';
        std::cout << "Enter /quit to leave room\n";
        inMenu_ = false;
        return true;
    }

    bool command_create()
    {
        std::cout << "Enter chat room name:" << std::endl;
        std::string room;
        std::getline(std::cin, room);

        if (room.empty()) {
            std::cout << "Wrong command syntax\n";
            printHelp();
            return true;
        }
        if (!chat_.createRoom(room)) {
            std::cout << "Failed to create " << room << '\n';
            printRooms();
            return true;
        }

        std::cout << "Created " << room << '\n';
        return true;
    }

    bool command_delete()
    {
        std::cout << "Enter chat room name:" << std::endl;
        std::string room;
        std::getline(std::cin, room);

        if (room.empty()) {
            std::cout << "Wrong command syntax\n";
            printHelp();
            return true;
        }
        if (!chat_.deleteRoom(room)) {
            std::cout << "Failed to delete " << room << '\n';
            printRooms();
            return true;
        }
        std::cout << "Deleted " << room << '\n';
        return true;
    }

private:
    ChatApp &chat_;
    bool inMenu_;
};

class ChatListenersCallback: public IFetchEventListeners {
public:

    ChatListenersCallback(EventFamilyFactory &factory) : eventFactory_(factory)
    {}

    ~ChatListenersCallback() = default;


    virtual void onEventListenersReceived(const std::vector<std::string> &eventListeners)
    {
        std::cout << "Kaa Demo found " << eventListeners.size() << " event listeners" << std::endl;
    }

    virtual void onRequestFailed()
    {
        std::cout << "Kaa Demo event listeners not found" << std::endl;
    }

private:
    EventFamilyFactory& eventFactory_;

};

class UserAttachCallback: public IUserAttachCallback {
public:

    UserAttachCallback(IKaaClient& client) : kaaClient_(client) { }

    virtual void onAttachSuccess()
    {
        kaaClient_.findEventListeners(std::list<std::string>( { CHAT_EVENT_FQN, CHAT_MESSAGE_FQN }),
                                      std::make_shared<ChatListenersCallback>(kaaClient_.getEventFamilyFactory()));
    }

    virtual void onAttachFailed(UserAttachErrorCode errorCode, const std::string& reason)
    {
        std::cout << "Kaa Demo attach failed (" << (int) errorCode << "): " << reason << std::endl;
    }

private:
    IKaaClient& kaaClient_;
};

int main(int argc, char *argv[])
{
    std::cout << "Event demo started" << std::endl;

    const std::string KAA_USER_ID("userid");
    const std::string KAA_USER_ACCESS_TOKEN("token");

    /*
     * Initialize the Kaa endpoint.
     */
    auto kaaClient = Kaa::newClient();

    /*
     * Run the Kaa endpoint.
     */
    kaaClient->start();

    ChatApp chat(kaaClient->getEventFamilyFactory());

    kaaClient->getEventFamilyFactory().getChat().addEventFamilyListener(chat);
    kaaClient->attachUser(KAA_USER_ID, KAA_USER_ACCESS_TOKEN, std::make_shared<UserAttachCallback>(*kaaClient));

    std::cout << "Endpoint ID: " << kaaClient->getEndpointKeyHash() << '\n';


    ChatMenu menu(chat);
    std::string userInput;

    menu.printHelp();
    menu.printRooms();
    do {
        std::getline(std::cin, userInput);
    } while (menu.process(userInput));

    /*
     * Stop the Kaa endpoint.
     */
    kaaClient->stop();

    return 0;
}
