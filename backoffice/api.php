<?php
// Lightweight REST Interface for clients (Android mobile application, frontend)
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

$task = $_GET['task'] ?? 'all';

$commerces_file = 'data/commerces.geojson';
$coupons_file = 'data/coupons.json';
$jeux_file = 'data/jeux.json';

// Utility helper to read file content safely
function read_json_data($file_path, $default = []) {
    if (file_exists($file_path)) {
        $data = json_decode(file_get_contents($file_path), true);
        if ($data !== null) return $data;
    }
    return $default;
}

switch($task) {
    case 'commerces':
        $payload = read_json_data($commerces_file, [
            "type" => "FeatureCollection",
            "features" => []
        ]);
        echo json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        break;
        
    case 'coupons':
        $payload = read_json_data($coupons_file, []);
        echo json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        break;
        
    case 'jeux':
        $payload = read_json_data($jeux_file, []);
        echo json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        break;
        
    case 'all':
    default:
        $commerces = read_json_data($commerces_file, [
            "type" => "FeatureCollection",
            "features" => []
        ]);
        $coupons = read_json_data($coupons_file, []);
        $jeux = read_json_data($jeux_file, []);
        
        $payload = [
            "status" => "success",
            "timestamp" => time(),
            "data" => [
                "commerces" => $commerces,
                "coupons" => $coupons,
                "jeux" => $jeux
            ]
        ];
        echo json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        break;
}
?>
