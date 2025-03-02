# LT-ItemFilter - Mod do Minecrafta

## 📌 Opis
**LT-ItemFilter** to modyfikacja do Minecrafta, która automatycznie filtruje przedmioty w inwentarzu. Mod umożliwia łatwe zarządzanie listą przedmiotów, które mają być zatrzymane, dzięki czemu nie musisz martwić się o przypadkowe wyrzucanie cennych itemów. Dzięki intuicyjnym komendom możesz szybko dodawać, usuwać przedmioty lub przełączać profile filtrów – wszystko to z poziomu czatu!

## 🛠️ Dostępne wersje
Sprawdź najnowsze wydania na [GitHubie](https://github.com/LordTricker/LT-ItemFilter/releases):

- 🟢 **1.18.2** – [Pobierz z GitHub](https://github.com/LordTricker/LT-ItemFilter/releases/download/1.18.2/ltitemfilter-1.0.0.jar)
- 🟢 **1.19.2** – [Pobierz z GitHub](https://github.com/LordTricker/LT-ItemFilter/releases/download/1.19.2/ltitemfilter-1.0.1.jar)
- 🔷 **1.21.1** – [Pobierz z GitHub](https://github.com/LordTricker/LT-ItemFilter/releases/download/1.21.1/ltitemfilter-1.0.0.jar)
- 🔴 **1.21.5** – Wkrótce

## ✨ Funkcje
- Automatyczne wyrzucanie przedmiotów niepożądanych z inwentarza.
- Zarządzanie listą dozwolonych przedmiotów przez profile.
- Łatwe dodawanie i usuwanie przedmiotów z filtra poprzez komendy.
- Przejrzysty interfejs z podświetlaniem przedmiotów (SHIFT) oraz wywoływanie filtru (Ctrl+G).
- Generowanie i ładowanie konfiguracji z pliku `ltitemfilter-config.json`.

## 📜 Komendy
Poniżej znajduje się lista dostępnych komend (skrócona wersja z pliku `messages.json`):

| Komenda                         | Opis                                                          |
|---------------------------------|---------------------------------------------------------------|
| `/lts filter`                   | Włącz/wyłącz filtr przedmiotów                                |
| `/lts add <item>`               | Dodaj przedmiot do filtra                                     |
| `/lts remove <item>`            | Usuń przedmiot z filtra                                       |
| `/lts list`                     | Wyświetl listę przedmiotów w aktualnym profilu                |
| `/lts profiles`                 | Lista dostępnych profili                                      |
| `/lts config save`              | Zapisz konfigurację                                           |
| `/lts config reload`            | Przeładuj konfigurację                                        |

Dodatkowo:
- **Ctrl+G** – skrót aktywujący filtrowanie (alternatywnie komenda `/lts filter`)
- **SHIFT** – podświetla przedmioty w inwentarzu


## 🛠 Konfiguracja
Po pierwszym uruchomieniu mod generuje plik konfiguracyjny:
```
%appdata%/.minecraft/config/ltitemfilter-config.json
```
W pliku można ręcznie edytować profile i ich zawartość.


## 🔧 Instalacja
1. Pobierz najnowszą wersję moda z [GitHuba](https://github.com/LordTricker/LT-ItemFilter/releases).
2. Umieść plik `.jar` w folderze `mods` w katalogu Minecrafta.
3. Uruchom grę – mod automatycznie wygeneruje plik konfiguracyjny w `%appdata%/.minecraft/config/ltitemfilter-config.json`.

## 🛠 Konfiguracja
Po pierwszym uruchomieniu mod generuje plik konfiguracyjny, w którym możesz ręcznie edytować listy przedmiotów oraz profile filtrów. Upewnij się, że struktura pliku nie zostanie zmieniona – modyfikuj tylko zawartość list (np. dodając identyfikatory przedmiotów).

## 👥 Autorzy
- **LordTricker** – Główny twórca moda
- **AdversTM** – pomoc przy podświetleniu przedmiotów
- **Mr. GPT** – Wsparcie merytoryczne 🤖

## 🌍 Kontakt
- **Strona:** [BlazeCode](https://blazecode.pl/)
- **Repozytorium:** [GitHub](https://github.com/LordTricker)
- **Serwer discord::** [Discord](https://dc.ltmods.pl/)
- **Discord:** LordTricker

---

> "Bierzcie i korzystajcie z tego wszyscy - to jest bowiem praca moja, która dla was została wykonana" ~LordTricker
