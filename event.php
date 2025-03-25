<?php
// Получаем данные POST-запроса
$data = json_decode(file_get_contents("php://input"), true);

$uuid = $data['uuid'];
$playerName = $data['playerName'];
$eventType = $data['eventType'];
$eventData = $data['eventData'];

// Логика для сохранения данных или вывода на сайт
// Например, запись в базу данных:

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "MavistMetricsMinecraft";

// Создаем соединение
$mysqli = new mysqli($servername, $username, $password, $dbname);

if ($mysqli->connect_error) {
    die("Connection failed: " . $mysqli->connect_error);
}

$stmt = $mysqli->prepare("INSERT INTO player_events (uuid, playerName, eventType, eventData) VALUES (?, ?, ?, ?)");
$stmt->bind_param("ssss", $uuid, $playerName, $eventType, $eventData);
$stmt->execute();
$stmt->close();
$mysqli->close();
?>
