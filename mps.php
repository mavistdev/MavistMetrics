<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "MavistMetricsMinecraft";

// Создаем соединение
$conn = new mysqli($servername, $username, $password, $dbname);

// Проверяем соединение
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$sql = "SELECT playerName, timePlayed, blocksBroken, blocksPlaced, deaths, last_update, ip FROM player_stats ORDER BY timePlayed DESC";
$result = $conn->query($sql);
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MavistMetrics</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container">
    <h1 class="my-4">Mavist Metrics - статистика по никам</h1>
    <table class="table table-striped">
        <thead>
        <tr>
            <th>Ник</th>
            <th>Сыграно (секунд)</th>
            <th>Блоков сломано</th>
            <th>Блоков поставлено</th>
            <th>Смертей</th>
            <!-- <th>ip</th> -->
            <th>Последняя запись от</th>
        </tr>
        </thead>
        <tbody>
        <?php
        if ($result->num_rows > 0) {
            while($row = $result->fetch_assoc()) {
                echo "<tr><td>" . htmlspecialchars($row["playerName"]) . "</td><td>" . $row["timePlayed"] . "</td><td>" . $row["blocksBroken"] . "</td><td>" . $row["blocksPlaced"] . "</td><td>" . $row["deaths"] . "</td><td>" . $row["last_update"] . "</td></tr>";
            }
        } else {
            echo "<tr><td colspan='6'>No data found</td></tr>";
        }
        $conn->close();
        ?>
        </tbody>
    </table>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>