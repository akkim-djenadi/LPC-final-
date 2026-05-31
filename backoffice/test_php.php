<?php
// Diagnostic tool to immediately spot hosting and file permission issues
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

echo "<!DOCTYPE html>
<html lang='fr'>
<head>
    <meta charset='UTF-8'>
    <title>Diagnostic Serveur - Le Petit Clapas</title>
    <script src='https://cdn.tailwindcss.com'></script>
    <link href='https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700&display=swap' rel='stylesheet'>
    <style>body { font-family: 'Plus Jakarta Sans', sans-serif; }</style>
</head>
<body class='bg-slate-50 text-slate-900 p-8'>
    <div class='max-w-3xl mx-auto bg-white p-8 rounded-2xl border border-slate-200 shadow-xl'>
        <div class='flex items-center space-x-3 border-b pb-4 mb-6'>
            <span class='text-4xl'>🛠️</span>
            <div>
                <h1 class='text-2xl font-black text-slate-800'>Diagnostic d'Hébergement PHP</h1>
                <p class='text-xs text-orange-500 font-bold uppercase tracking-wider'>Le Petit Clapas - Montpellier</p>
            </div>
        </div>";

echo "<div class='space-y-6'>
        <section>
            <h2 class='text-sm font-bold uppercase text-slate-400 tracking-wider mb-2'>Version du Serveur</h2>
            <div class='bg-slate-50 p-4 rounded-xl border flex items-center justify-between'>
                <span class='text-xs font-semibold text-slate-700'>Version Active de PHP :</span>
                <span class='px-3 py-1 bg-indigo-50 text-indigo-700 rounded-lg text-xs font-mono font-bold'>" . htmlspecialchars(phpversion()) . "</span>
            </div>
        </section>";

$items_to_check = [
    'data/' => 'directory',
    'data/commerces.geojson' => 'file',
    'data/coupons.json' => 'file',
    'data/jeux.json' => 'file',
    'data/users.json' => 'file',
    'header.php' => 'file',
    'footer.php' => 'file',
    'api.php' => 'file',
    'index.php' => 'file',
    'comptes.php' => 'file',
    'commerces.php' => 'file',
    'commerces_liste.php' => 'file'
];

echo "<section>
        <h2 class='text-sm font-bold uppercase text-slate-400 tracking-wider mb-3'>Permissions d'Écriture et Lecture</h2>
        <div class='space-y-2'>";

foreach ($items_to_check as $path => $type) {
    if ($type === 'directory') {
        $exists = is_dir($path);
    } else {
        $exists = file_exists($path);
    }
    
    if (!$exists) {
        echo "<div class='flex items-center justify-between bg-rose-50 border border-rose-200 p-3 rounded-lg text-xs'>
                <span class='font-mono text-rose-800 font-bold'>$path</span>
                <span class='px-2 py-0.5 bg-rose-200 text-rose-800 rounded font-semibold uppercase text-[9px]'>Fichier Manquant !</span>
              </div>";
    } else {
        $readable = is_readable($path);
        $writable = is_writable($path);
        
        $color_class = ($readable && $writable) ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-amber-50 border-amber-200 text-amber-800';
        $status_label = ($readable && $writable) ? 'Lecture/Écriture OK ✅' : 'Lecture Seule ⚠️';
        
        echo "<div class='flex items-center justify-between border p-3 rounded-lg text-xs $color_class'>
                <span class='font-mono font-bold'>$path</span>
                <span class='font-semibold uppercase text-[10px]'>$status_label</span>
              </div>";
    }
}
echo "</div></section>";

echo "<section>
        <h2 class='text-sm font-bold uppercase text-slate-400 tracking-wider mb-3'>Vérification de l'Intégrité JSON</h2>
        <div class='space-y-2'>";

foreach (['data/commerces.geojson', 'data/coupons.json', 'data/jeux.json', 'data/users.json'] as $path) {
    if (file_exists($path)) {
        $content = file_get_contents($path);
        $decoded = json_decode($content, true);
        if ($decoded === null) {
            echo "<div class='bg-rose-50 border border-rose-300 p-3 rounded-lg text-xs text-rose-800'>
                    <div class='font-bold font-mono'>$path</div>
                    <div class='mt-1 text-[11px] text-rose-700 font-semibold'>❌ JSON Corrompu ! Erreur : " . htmlspecialchars(json_last_error_msg()) . "</div>
                  </div>";
        } else {
            $count = 0;
            if (isset($decoded['features'])) {
                $count = count($decoded['features']);
                $meta = "FeatureCollection ($count établissements)";
            } else {
                $count = count($decoded);
                $meta = "Array ($count entrées)";
            }
            echo "<div class='bg-slate-50 border border-slate-200 p-3 rounded-lg text-xs flex justify-between items-center text-slate-700'>
                    <span class='font-mono font-bold'>$path</span>
                    <span class='font-semibold text-slate-600 bg-white px-2 py-0.5 rounded border'>$meta</span>
                  </div>";
        }
    }
}
echo "</div></section>";

echo "<div class='pt-4 border-t text-xs text-slate-400 leading-relaxed'>
        <p>💡 <strong>Conseil :</strong> Si certains répertoires ou fichiers ne sont pas modifiables, connectez-vous à votre client FTP (FileZilla) et effectuez un <strong>CHMOD 777</strong> sur le dossier <code class='bg-slate-100 px-1 rounded font-mono text-slate-800'>data/</code> et les fichiers qu'il contient.</p>
      </div>
    </div>
</body>
</html>";
?>
