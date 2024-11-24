# TaskShell

TaskShell is a lightweight command-line tool for managing and organizing your daily tasks. Built with Spring Boot, it offers flexible task listing, filtering, and management with detailed or tabular views. Task data is stored in a portable JSON format for simplicity.

## Features

- List all tasks or filter by date.
- Display tasks in simple, detailed, or tabular formats.
- Apply filters to tasks before listing them (.e.g, By date, By status )
- Add new tasks with customizable due dates and statuses.
- Task data is saved in a JSON file in device's home directory for easy portability.
- Move undone tasks from today to tomorrow or using any dates
- Modify task description or status
- Delete tasks

## Getting Started

### Prerequisites

- Java 17 or higher
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

## Commands

### List Tasks

Display list of created tasks

<details><summary>List tasks due for today</summary>

```bash
taskcli list
```
</details>

<details><summary>List tasks due in any specified date</summary>

```bash
taskcli list --date 24/11/2024
```
</details>

<details><summary>List all created tasks without any filters </summary>

```bash
taskcli list --a
```
</details>

