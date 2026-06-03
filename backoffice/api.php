<?php
// Enable explicit error reporting to immediately diagnose hosting-level issues on production
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// Lightweight REST Interface for clients (Android mobile application, frontend)
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

// Using older PHP-compatible approach for maximum hosting portability (compat PHP 5.4+)
$task = isset($_GET['task']) ? $_GET['task'] : 'all';

$commerces_file = 'data/commerces.geojson';
$coupons_file = 'data/coupons.json';
$jeux_file = 'data/jeux.json';

// Diagnostic check helper for FTP deployments
$diagnostics = [];
foreach (['data/commerces.geojson' => $commerces_file, 'data/coupons.json' => $coupons_file, 'data/jeux.json' => $jeux_file] as $name => $path) {
    if (!file_exists($path)) {
        $diagnostics['missing_files'][] = $name;
    } elseif (!is_readable($path)) {
        $diagnostics['unreadable_files'][] = $name;
    }
}

// Utility helper to read file content safely
function read_json_data($file_path, $default = array()) {
    if (file_exists($file_path) && is_readable($file_path)) {
        $content = file_get_contents($file_path);
        if ($content !== false && trim($content) !== '') {
            $data = json_decode($content, true);
            if ($data !== null) return $data;
        }
    }
    return $default;
}

function ultra_clean($str) {
    $str = mb_strtolower($str, 'UTF-8');
    $str = str_replace('&', 'et', $str);
    $transliterator = array(
        'à'=>'a', 'á'=>'a', 'â'=>'a', 'ã'=>'a', 'ä'=>'a', 'å'=>'a', 'æ'=>'ae', 'ç'=>'c',
        'è'=>'e', 'é'=>'e', 'ê'=>'e', 'ë'=>'e', 'ì'=>'i', 'í'=>'i', 'î'=>'i', 'ï'=>'i',
        'ð'=>'d', 'ñ'=>'n', 'ò'=>'o', 'ó'=>'o', 'ô'=>'o', 'õ'=>'o', 'ö'=>'o', 'ø'=>'o',
        'ù'=>'u', 'ú'=>'u', 'û'=>'u', 'ü'=>'u', 'ý'=>'y', 'ÿ'=>'y', 'œ'=>'oe', 'ß'=>'ss',
        '’'=>'', '\''=>''
    );
    $str = strtr($str, $transliterator);
    $str = preg_replace('/[^a-z0-9]/', '', $str);
    return $str;
}

function remove_french_prefixes_str($str) {
    return preg_replace('/^(le|la|les|un|une|du|de|des|au|aux|l|d)/', '', $str);
}

function find_closest_image($name) {
    static $images = null;
    if ($images === null) {
        $file = __DIR__ . '/data/images_list.txt';
        if (file_exists($file) && is_readable($file)) {
            $content = file_get_contents($file);
            $images = preg_split('/\r\n|\r|\n/', $content);
            $images = array_filter(array_map('trim', $images));
        } else {
            $images = array();
        }
    }
    
    if (empty($images)) {
        return '';
    }
    
    $clean_name = ultra_clean($name);
    $clean_name_noprefix = remove_french_prefixes_str($clean_name);
    
    $fuzzy_match = '';
    
    foreach ($images as $img) {
        $img_no_ext = pathinfo($img, PATHINFO_FILENAME);
        $clean_img = ultra_clean($img_no_ext);
        $clean_img_noprefix = remove_french_prefixes_str($clean_img);
        
        // Match 1: Extract match
        if ($clean_name === $clean_img) {
            return $img;
        }
        
        // Match 2: Prefix-removed match
        if (!empty($clean_name_noprefix) && !empty($clean_img_noprefix) && $clean_name_noprefix === $clean_img_noprefix) {
            return $img;
        }
        
        // Match 3: Check if either is a substring of another
        if (empty($fuzzy_match)) {
            if (!empty($clean_name) && !empty($clean_img)) {
                if (strpos($clean_name, $clean_img) !== false || strpos($clean_img, $clean_name) !== false) {
                    $fuzzy_match = $img;
                }
            }
        }
    }
    
    return $fuzzy_match;
}

// Automatically resolve and inject correct Raw GitHub content images dynamically on-the-fly
function resolve_commerces_images($payload) {
    if (isset($payload['features']) && is_array($payload['features'])) {
        foreach ($payload['features'] as $k => $f) {
            if (isset($f['properties'])) {
                $img_name = $f['properties']['github_image'] ?? '';
                if (empty($img_name)) {
                    $existing_img = $f['properties']['image_url'] ?? '';
                    if (!empty($existing_img) && strpos($existing_img, 'http') === false) {
                        $img_name = basename($existing_img);
                    }
                }
                if (empty($img_name)) {
                    $img_name = find_closest_image($f['properties']['name'] ?? '');
                }
                if (empty($img_name)) {
                    $slug = mb_strtolower($f['properties']['name'] ?? '', 'UTF-8');
                    $slug = preg_replace('/[\s\'’\-–—,.\/\\\]+/', '_', $slug);
                    $slug = trim($slug, '_');
                    $img_name = $slug . '.jpg';
                }
                $payload['features'][$k]['properties']['image_url'] = "https://raw.githubusercontent.com/akkim-djenadi/LPC-final-/main/images_commerces/" . urlencode($img_name);
            }
        }
    }
    return $payload;
}

switch($task) {
    case 'register':
        $users_file = 'data/users.json';
        $users = read_json_data($users_file, array());
        
        // Grab inputs from body (JSON) or $_POST or $_GET
        $input = json_decode(file_get_contents('php://input'), true);
        $name = trim($input['name'] ?? $_POST['name'] ?? $_GET['name'] ?? '');
        $email = trim($input['email'] ?? $_POST['email'] ?? $_GET['email'] ?? '');
        $password = trim($input['password'] ?? $_POST['password'] ?? $_GET['password'] ?? '');
        
        if (empty($name) || empty($email) || empty($password)) {
            echo json_encode(array("status" => "error", "message" => "Tous les champs (nom, email, mot de passe) sont requis."));
            break;
        }
        
        $exists = false;
        foreach ($users as $u) {
            if (strtolower($u['email']) === strtolower($email)) {
                $exists = true;
                break;
            }
        }
        
        if ($exists) {
            echo json_encode(array("status" => "error", "message" => "Un compte avec cette adresse email existe déjà."));
        } else {
            $user_role = (strtolower($email) === 'a.djenadi34@gmail.com') ? "admin" : "client";
            $new_user = array(
                "id" => "usr_" . rand(100000, 999999),
                "name" => $name,
                "email" => $email,
                "password" => $password,
                "role" => $user_role,
                "merchant_id" => null
            );
            $users[] = $new_user;
            file_put_contents($users_file, json_encode($users, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            echo json_encode(array("status" => "success", "user" => $new_user));
        }
        break;

    case 'login':
        $users_file = 'data/users.json';
        $users = read_json_data($users_file, array());
        
        $input = json_decode(file_get_contents('php://input'), true);
        $email = trim($input['email'] ?? $_POST['email'] ?? $_GET['email'] ?? '');
        $password = trim($input['password'] ?? $_POST['password'] ?? $_GET['password'] ?? '');
        
        if (empty($email) || empty($password)) {
            echo json_encode(array("status" => "error", "message" => "L'email et le mot de passe sont requis."));
            break;
        }
        
        $found = null;
        foreach ($users as $key => $u) {
            if (strtolower($u['email']) === strtolower($email) && $u['password'] === $password) {
                // Force a.djenadi34@gmail.com to be an admin
                if (strtolower($email) === 'a.djenadi34@gmail.com' && $u['role'] !== 'admin') {
                    $users[$key]['role'] = 'admin';
                    $u['role'] = 'admin';
                    file_put_contents($users_file, json_encode($users, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
                }
                $found = $u;
                break;
            }
        }
        
        if ($found) {
            echo json_encode(array("status" => "success", "user" => $found));
        } else {
            echo json_encode(array("status" => "error", "message" => "Identifiants inconnus ou mot de passe incorrect."));
        }
        break;

    case 'google_sso':
        $users_file = 'data/users.json';
        $users = read_json_data($users_file, array());
        
        $input = json_decode(file_get_contents('php://input'), true);
        $email = trim($input['email'] ?? $_POST['email'] ?? $_GET['email'] ?? '');
        $name = trim($input['name'] ?? $_POST['name'] ?? $_GET['name'] ?? '');
        
        if (empty($email)) {
            echo json_encode(array("status" => "error", "message" => "L'adresse email Google SSO est requise."));
            break;
        }
        
        $found = null;
        foreach ($users as $key => $u) {
            if (strtolower($u['email']) === strtolower($email)) {
                // Force a.djenadi34@gmail.com to be an admin
                if (strtolower($email) === 'a.djenadi34@gmail.com' && $u['role'] !== 'admin') {
                    $users[$key]['role'] = 'admin';
                    $u['role'] = 'admin';
                    file_put_contents($users_file, json_encode($users, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
                }
                $found = $u;
                break;
            }
        }
        
        if ($found) {
            echo json_encode(array("status" => "success", "user" => $found));
        } else {
            // Register on-the-fly
            $user_role = (strtolower($email) === 'a.djenadi34@gmail.com') ? "admin" : "client";
            $new_user = array(
                "id" => "usr_" . rand(100000, 999999),
                "name" => !empty($name) ? $name : explode('@', $email)[0],
                "email" => $email,
                "password" => "sso_google_" . rand(100000, 999999),
                "role" => $user_role,
                "merchant_id" => null
            );
            $users[] = $new_user;
            file_put_contents($users_file, json_encode($users, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            echo json_encode(array("status" => "success", "user" => $new_user));
        }
        break;

    case 'update_profile':
        $users_file = 'data/users.json';
        $users = read_json_data($users_file, array());
        
        $input = json_decode(file_get_contents('php://input'), true);
        $userId = trim($input['id'] ?? $_POST['id'] ?? $_GET['id'] ?? '');
        $name = trim($input['name'] ?? $_POST['name'] ?? $_GET['name'] ?? '');
        $email = trim($input['email'] ?? $_POST['email'] ?? $_GET['email'] ?? '');
        
        if (empty($userId) || empty($name) || empty($email)) {
            echo json_encode(array("status" => "error", "message" => "L'ID, le nom complet et l'adresse email sont requis."));
            break;
        }
        
        $email_taken = false;
        foreach ($users as $u) {
            if ($u['id'] !== $userId && strtolower($u['email']) === strtolower($email)) {
                $email_taken = true;
                break;
            }
        }
        
        if ($email_taken) {
            echo json_encode(array("status" => "error", "message" => "Cette adresse email est déjà utilisée par un autre compte."));
            break;
        }
        
        $updated_user = null;
        foreach ($users as $key => $u) {
            if ($u['id'] === $userId) {
                $users[$key]['name'] = $name;
                $users[$key]['email'] = $email;
                
                // If it's a.djenadi34@gmail.com, force 'admin' status
                if (strtolower($email) === 'a.djenadi34@gmail.com' || $userId === 'usr_djenadi') {
                    $users[$key]['role'] = 'admin';
                    $users[$key]['email'] = 'a.djenadi34@gmail.com';
                }
                
                $updated_user = $users[$key];
                break;
            }
        }
        
        if ($updated_user) {
            file_put_contents($users_file, json_encode($users, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
            echo json_encode(array("status" => "success", "user" => $updated_user));
        } else {
            echo json_encode(array("status" => "error", "message" => "Utilisateur non trouvé. Impossible de modifier les informations."));
        }
        break;

    case 'commerces':
        $payload = read_json_data($commerces_file, array(
            "type" => "FeatureCollection",
            "features" => array()
        ));
        $payload = resolve_commerces_images($payload);
        echo json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        break;
        
    case 'coupons':
        $payload = read_json_data($coupons_file, array());
        echo json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        break;
        
    case 'jeux':
        $payload = read_json_data($jeux_file, array());
        echo json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        break;
        
    case 'all':
    default:
        $commerces = read_json_data($commerces_file, array(
            "type" => "FeatureCollection",
            "features" => array()
        ));
        $commerces = resolve_commerces_images($commerces);
        $coupons = read_json_data($coupons_file, array());
        $jeux = read_json_data($jeux_file, array());
        
        $payload = array(
            "status" => "success",
            "timestamp" => time(),
            "diagnostics" => !empty($diagnostics) ? $diagnostics : "all_okay",
            "data" => array(
                "commerces" => $commerces,
                "coupons" => $coupons,
                "jeux" => $jeux
            )
        );
        echo json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        break;
}
?>
