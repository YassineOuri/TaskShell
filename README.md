# TaskShell

TaskShell is a lightweight command-line tool for managing and organizing your daily tasks. Built with Spring Boot, it offers flexible task listing, filtering, and management with detailed or tabular views. Task data is stored in a portable JSON format for simplicity.

## Features

- List all tasks or filter by date.
- Display tasks in simple, detailed, or tabular formats.
- Add new tasks with customizable due dates and statuses.
- Task data is saved in a JSON file for easy portability.

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven

### Installation

1. **Clone the repository:**

    ```bash
    git clone https://github.com/your-username/TaskShell.git
    cd TaskShell
    ```

2. **Build the project:**

    ```bash
    mvn clean install
    ```

3. **Run the application:**

    ```bash
    java -jar target/TaskShell-1.0.jar
    ```

## Usage

### List Tasks

```bash
java -jar target/TaskShell-1.0.jar list
