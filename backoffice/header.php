<?php
// PHP Backoffice Header
$current_page = basename($_SERVER['PHP_SELF']);
?>
<!DOCTYPE html>
<html lang="fr" class="h-full bg-slate-50">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Le Petit Clapas - Back Office d'Administration des Commerces</title>
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- Material Design Icons and Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" />
    <style>
        body {
            font-family: 'Plus Jakarta Sans', sans-serif;
        }
        .code-font {
            font-family: 'JetBrains Mono', monospace;
        }
        .material-symbols-rounded {
          font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24;
          vertical-align: middle;
        }
    </style>
</head>
<body class="h-full flex flex-col md:flex-row">

    <!-- SIDEBAR FOR DESKTOP -->
    <aside class="w-full md:w-64 bg-slate-900 text-slate-100 flex-shrink-0 flex flex-col border-r border-slate-800">
        <!-- Logo and App Title -->
        <div class="p-6 border-b border-slate-800 flex items-center space-x-3">
            <span class="text-3xl">🗺️</span>
            <div>
                <h1 class="text-lg font-bold tracking-tight text-white">Le Petit Clapas</h1>
                <p class="text-xs text-orange-400 font-semibold uppercase tracking-wider">Back Office Admin</p>
            </div>
        </div>

        <!-- Navigation Menu -->
        <nav class="flex-1 px-4 py-6 space-y-1.5 overflow-y-auto">
            <a href="index.php" class="flex items-center space-x-3 px-4 py-3 rounded-xl transition duration-150 <?php echo ($current_page == 'index.php') ? 'bg-orange-500 text-white font-semibold' : 'text-slate-300 hover:bg-slate-800 hover:text-white'; ?>">
                <span class="material-symbols-rounded">dashboard</span>
                <span class="text-sm">Tableau de bord</span>
            </a>
            
            <a href="commerces.php" class="flex items-center space-x-3 px-4 py-3 rounded-xl transition duration-150 <?php echo ($current_page == 'commerces.php') ? 'bg-orange-500 text-white font-semibold' : 'text-slate-300 hover:bg-slate-800 hover:text-white'; ?>">
                <span class="material-symbols-rounded">storefront</span>
                <span class="text-sm">Commerces</span>
            </a>
            
            <a href="donnees.php" class="flex items-center space-x-3 px-4 py-3 rounded-xl transition duration-150 <?php echo ($current_page == 'donnees.php') ? 'bg-orange-500 text-white font-semibold' : 'text-slate-300 hover:bg-slate-800 hover:text-white'; ?>">
                <span class="material-symbols-rounded">database</span>
                <span class="text-sm">Données</span>
            </a>
            
            <a href="commerces_liste.php" class="flex items-center space-x-3 px-4 py-3 rounded-xl transition duration-150 <?php echo ($current_page == 'commerces_liste.php') ? 'bg-orange-500 text-white font-semibold' : 'text-slate-300 hover:bg-slate-800 hover:text-white'; ?>">
                <span class="material-symbols-rounded">view_list</span>
                <span class="text-sm">Liste & Édition de lot</span>
            </a>
            
            <a href="jeux.php" class="flex items-center space-x-3 px-4 py-3 rounded-xl transition duration-150 <?php echo ($current_page == 'jeux.php') ? 'bg-orange-500 text-white font-semibold' : 'text-slate-300 hover:bg-slate-800 hover:text-white'; ?>">
                <span class="material-symbols-rounded">explore</span>
                <span class="text-sm">Configuration Jeux</span>
            </a>
            
            <a href="coupons.php" class="flex items-center space-x-3 px-4 py-3 rounded-xl transition duration-150 <?php echo ($current_page == 'coupons.php') ? 'bg-orange-500 text-white font-semibold' : 'text-slate-300 hover:bg-slate-800 hover:text-white'; ?>">
                <span class="material-symbols-rounded">confirmation_number</span>
                <span class="text-sm">Gestion des Coupons</span>
            </a>

            <a href="comptes.php" class="flex items-center space-x-3 px-4 py-3 rounded-xl transition duration-150 <?php echo ($current_page == 'comptes.php' || $current_page == 'comptes.php?tab=clients' || $current_page == 'comptes.php?tab=commercants' || $current_page == 'comptes.php?tab=admins') ? 'bg-orange-500 text-white font-semibold' : 'text-slate-300 hover:bg-slate-800 hover:text-white'; ?>">
                <span class="material-symbols-rounded">manage_accounts</span>
                <span class="text-sm">Gestion des Comptes</span>
            </a>
        </nav>

        <!-- System & GitHub Quick Info -->
        <div class="p-4 border-t border-slate-800 text-xs text-slate-400 space-y-2">
            <div class="flex items-center justify-between">
                <span>Statut :</span>
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">Actif FTP</span>
            </div>
            <div>
                <p class="font-medium text-slate-300">Répertoire GitHub lié :</p>
                <a href="https://github.com/akkim-djenadi/le-petit-clapas-/tree/main/images_commerces" target="_blank" class="text-blue-400 truncate block hover:underline">images_commerces ↗</a>
            </div>
        </div>
    </aside>

    <!-- MAIN BODY CONTENT CONTAINER -->
    <main class="flex-1 flex flex-col overflow-hidden">
        <!-- TOP NAV BAR -->
        <header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6 md:px-8 py-4 z-10">
            <div class="flex items-center space-x-3">
                <span class="text-slate-400 text-sm hidden md:inline">Système d'administration localisé de Montpellier</span>
            </div>
            <div class="flex items-center space-x-4">
                <div class="flex flex-col text-right">
                    <span class="text-xs font-semibold text-slate-900"><?php echo htmlspecialchars("a.djenadi34@gmail.com"); ?></span>
                    <span class="text-[10px] text-slate-500">SuperAdministrateur</span>
                </div>
                <div class="h-9 w-9 rounded-full bg-slate-200 border-2 border-orange-500 flex items-center justify-center font-bold text-slate-800">
                    AD
                </div>
            </div>
        </header>

        <!-- ROUTED PAGE WRAPPER -->
        <div class="flex-1 overflow-y-auto p-6 md:p-8">
