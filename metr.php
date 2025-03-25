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

$data = json_decode(file_get_contents('php://input'), true);

$uuid = $conn->real_escape_string($data['uuid']);
$playerName = $conn->real_escape_string($data['playerName']);
$timePlayed = (int)$data['timePlayed'];
$blocksBroken = (int)$data['blocksBroken'];
$blocksPlaced = (int)$data['blocksPlaced'];
$deaths = (int)$data['deaths'];
$lastIP = $conn->real_escape_string($data['lastIP']);
$sprint = (int)$data['sprint'];
$tp = (int)$data['teleport'];
$sneak = (int)$data['sneak'];
$gamemodec = (int)$data['gamemodechanged'];
$invop = (int)$data['invop'];

// Проверяем наличие записи с данным UUID в базе данных
$sql = "SELECT * FROM player_stats WHERE uuid = '$uuid'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    // Если запись существует, обновляем ее
    $row = $result->fetch_assoc();
    
    $newTimePlayed = $row['timePlayed'] + $timePlayed;
    $newBlocksBroken = $row['blocksBroken'] + $blocksBroken;
    $newBlocksPlaced = $row['blocksPlaced'] + $blocksPlaced;
    $newDeaths = $row['deaths'] + $deaths;
    $nsprint = $row['sprint'] + $sprint;
    $nsneak = $row['sneak'] + $sneak;
    $ntp = $row['tp'] + $tp;
    $ngm = $row['gamemodec'] + $gamemodec;
    $ninv = $row['invop'] + $invop;

    $updateSql = "UPDATE player_stats 
                  SET playerName = '$playerName', 
                      timePlayed = $newTimePlayed, 
                      blocksBroken = $newBlocksBroken, 
                      blocksPlaced = $newBlocksPlaced, 
                      deaths = $newDeaths,
                      ip = '$lastIP',
                      sprint = '$nsprint',
                      tp = '$ntp',
                      sneak = '$nsneak',
                      gamemodec = '$ngm',
                      invop = '$ninv',
                      last_update = NOW()
                  WHERE uuid = '$uuid'";
    
    if ($conn->query($updateSql) === TRUE) {
        echo "Record updated successfully";
    } else {
        echo "Error updating record: " . $conn->error;
    }
} else {
    // Если записи нет, создаем новую
    $insertSql = "INSERT INTO player_stats (uuid, playerName, timePlayed, blocksBroken, blocksPlaced, deaths, ip, sprint, tp, sneak, gamemodec, invop) 
                  VALUES ('$uuid', '$playerName', $timePlayed, $blocksBroken, $blocksPlaced, $deaths, '$lastIP', '$sprint', '$tp', '$sneak', '$gamemodec', '$invop')";
    
    if ($conn->query($insertSql) === TRUE) {
        echo "New record created successfully";
    } else {
        echo "Error: " . $insertSql . "<br>" . $conn->error;
    }
}

$conn->close();
?>