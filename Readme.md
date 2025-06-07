# Wordle Backend

This is the backend implementation for a multiplayer Wordle game. It is built using **Java**, **Spring Boot**, and **WebSocket** for real-time communication. The backend handles game creation, player management, and game state updates.

## Features

- **WebSocket Communication**: Real-time updates for players.
- **Room Management**: Create, join, and manage game rooms.
- **Player Management**: Add, remove, and track players in a game.
- **Game State Management**: Start games, track scores, and handle game completion.
- **Custom Exceptions**: Clear error handling with specific exceptions.

## Technologies Used

- **Java 17**
- **Spring Boot**
- **WebSocket**
- **Gradle** (Build Tool)
- **Lombok** (For reducing boilerplate code)
- **Jackson** (For JSON serialization/deserialization)

## Project Structure

- `room`: Handles room creation, player management, and game logic.
- `messages`: Defines incoming and outgoing WebSocket messages.
- `error`: Custom exceptions for error handling.
- `session`: Manages WebSocket sessions.