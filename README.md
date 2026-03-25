# Movie Analyzer – OMDB Client/Server with Recommendations

## What is this application?

The **Movie Analyzer** is a client‑server application that allows users to search for movies using the [OMDB API](http://www.omdbapi.com/) and receive personalized recommendations based on genre similarity. The server is built with Java NIO (non‑blocking I/O) to handle multiple clients concurrently, and it caches results to minimize external API calls. The client provides a simple console interface with the option to display movie posters in a separate GUI window.

---

## Features

- **Search by Title** – Retrieve detailed information about a movie using its title.
- **Search by IMDb ID** – Use the IMDb identifier (e.g., `tt0133093` for *The Matrix*) to get movie details.
- **Genre‑Based Recommendations** – Given a movie title, the server analyzes its genres and returns up to 5 movies from its cache that share the most genres, sorted by similarity and rating.
- **Persistent Cache** – All successful API responses are saved locally in a `cache` directory, reducing redundant network requests and improving performance.
- **Non‑Blocking Server** – Uses `java.nio.channels.Selector` to handle many connections with a single thread, and offloads command processing to a thread pool to keep the event loop responsive.
- **JSON Communication** – All messages between client and server are sent as JSON, making the protocol language‑agnostic and easy to extend.
- **Poster Display (GUI)** – When a movie has a poster URL, the client automatically opens a small window with the poster image.

---

## Architecture Overview


- **Client** – A console application that reads user commands, sends them to the server, and prints the formatted JSON responses. If the response contains a movie with a poster URL, it launches a `Swing` window to display the image.
- **Server** – A NIO server that:
  1. Accepts incoming connections.
  2. Reads commands line by line (delimited by newline).
  3. Forwards each command to a worker thread for processing.
  4. Sends back the result as a JSON string.
- **Command Processor** – Parses the command line into a `Command` object (using the **Command pattern**) and executes it using the `MovieService`.
- **MovieService** – The core business logic:
  - `OMDbMovieService` implements the interface and handles API calls, caching, and validation.
  - `RecommendationService` (implemented by `GenreBasedRecommendationService`) calculates genre similarity and returns the best matches.
- **CacheService** – Manages the file‑based cache. Stores raw JSON responses and retrieves them when available.
- **Configuration** – `ServerConfig` loads settings from `server.properties` or environment variables (API key, port, cache directory, etc.).
- **Logging** – `LoggerUtil` writes errors and warnings to a log file.

---

## Getting Started

### Prerequisites
- **Java 11** or higher
- **OMDB API key** (free at [http://www.omdbapi.com/apikey.aspx](http://www.omdbapi.com/apikey.aspx))
- Internet connection for API calls

### Setting up the API key
The server reads the API key from the environment variable `OMDB_API_KEY`. Set it before starting the server:

```bash
# Linux / macOS
export OMDB_API_KEY=your_key_here

# Windows Command Prompt
set OMDB_API_KEY=your_key_here

# Windows PowerShell
$env:OMDB_API_KEY="your_key_here"
```
---

## Available command for the client

- **get-movie <movie_title>** - Retrieve detailed information about a movie using its title.
- **recommend <movie_title>** – Given a movie title, the server analyzes its genres and returns up to 5 movies from its cache that share the most genres, sorted by similarity and rating.
- **get-movie-id <movie_IMDb id>** – Use the IMDb identifier (e.g., `tt0133093` for *The Matrix*) to get movie details.
- **exit**