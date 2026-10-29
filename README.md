# Cinejoy Launcher (Firefox)

Mini-appli Android TV sans interface : au lancement, elle lit l'adresse du site dans `url.txt`
(sur GitHub), la mémorise, puis ouvre le site dans Firefox.

## Installation
1. Crée un dépôt GitHub **public** (nom neutre) et mets-y tout ce dossier (y compris `.github/`).
2. Onglet **Actions** → « Build APK » se lance tout seul (sinon *Run workflow*).
   Télécharge l'artefact `CinejoyLauncher` (zip contenant `CinejoyLauncher.apk`).
3. Sur la TV : Firefox installé (APK arm-v7a), puis
   `adb install -r CinejoyLauncher.apk`
4. L'appli « Cinejoy (Firefox) » apparaît dans Free My TV → Toutes les apps.

## Si le site change d'adresse
Modifie la ligne dans `url.txt` sur github.com (même depuis le téléphone). Pas besoin de recompiler.
La TV la récupère au prochain lancement (le cache GitHub peut retarder de ~5 min).
Hors ligne, l'appli utilise la dernière adresse connue.

## Notes
- Chaque build est signé avec une clé neuve : pour mettre à jour l'APK, désinstalle d'abord
  l'ancien (`adb uninstall io.cinejoy.launcher`).
- Pour uBlock Origin : dans Firefox, menu → Modules complémentaires.
