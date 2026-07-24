# TrainingIT — ghid complet de instalare și pornire

TrainingIT este o aplicație web full-stack pentru administrarea cursurilor și a
relației cu clienții. Aplicația include un catalog public, autentificare pentru
clienți și administratori, achiziții și recenzii, programarea ședințelor,
facturare, administrarea contactelor și funcții AI opționale.

## Cuprins

1. [Tehnologii și arhitectură](#tehnologii-și-arhitectură)
2. [Cerințe](#cerințe)
3. [Pornire rapidă pe Windows](#pornire-rapidă-pe-windows)
4. [Pornire pe macOS sau Linux](#pornire-pe-macos-sau-linux)
5. [Conturi demonstrative](#conturi-demonstrative)
6. [Configurare](#configurare)
7. [Verificarea aplicației](#verificarea-aplicației)
8. [Build și teste](#build-și-teste)
9. [Oprirea aplicației](#oprirea-aplicației)
10. [Depanare](#depanare)
11. [Structura proiectului](#structura-proiectului)

## Tehnologii și arhitectură

- **Frontend:** Next.js 16, React 19, TypeScript și Tailwind CSS 4
- **Backend:** Java 17+, Spring Boot 3.5 și Maven
- **Bază de date:** MariaDB/MySQL, accesată prin JDBC și HikariCP
- **AI opțional:** Anthropic Claude
- **Port frontend:** `3000`
- **Port backend:** `8080`
- **Port implicit MariaDB/MySQL:** `3306`

Fluxul aplicației este:

```text
Browser
  └── Next.js: http://localhost:3000
        └── REST API: http://localhost:8080/api
              └── MariaDB: localhost:3306/crm_training
```

## Cerințe

Instalează următoarele programe înainte de prima pornire:

- **JDK 17 sau mai nou** — verificare: `java -version`
- **Node.js 20.9 sau mai nou** — verificare: `node --version`
- **npm** — verificare: `npm --version`
- **MariaDB sau MySQL** — poate fi folosit și serverul MySQL inclus în XAMPP
- **Git**, dacă proiectul este descărcat dintr-un repository

Nu este obligatorie instalarea globală a Maven. Proiectul include Maven Wrapper
prin `mvnw.cmd` și `mvnw`.

La prima utilizare, Maven și npm au nevoie de acces la internet pentru
descărcarea dependențelor.

## Pornire rapidă pe Windows

Toate comenzile de mai jos se execută din rădăcina proiectului:

```powershell
cd D:\TrainingIT_site
```

### 1. Pornește MariaDB/MySQL

Dacă folosești XAMPP, deschide **XAMPP Control Panel** și pornește modulul
**MySQL**.

Dacă folosești MariaDB/MySQL instalat ca serviciu Windows, pornește serviciul
corespunzător și verifică faptul că ascultă pe portul `3306`.

### 2. Inițializează baza de date

Aceste două scripturi se rulează în ordine și numai la configurarea unei baze
de date noi:

```powershell
mysql --default-character-set=utf8mb4 -u root -p -e "source sql/schema.sql"
mysql --default-character-set=utf8mb4 -u root -p -e "source sql/seed-data.sql"
```

Introdu parola utilizatorului `root` când este solicitată. Pentru configurația
implicită XAMPP, unde utilizatorul `root` nu are parolă, elimină parametrul
`-p`:

```powershell
mysql --default-character-set=utf8mb4 -u root -e "source sql/schema.sql"
mysql --default-character-set=utf8mb4 -u root -e "source sql/seed-data.sql"
```

Dacă executabilul `mysql` nu este în `PATH`, folosește calea completă:

```powershell
& "C:\xampp\mysql\bin\mysql.exe" --default-character-set=utf8mb4 -u root -e "source sql/schema.sql"
& "C:\xampp\mysql\bin\mysql.exe" --default-character-set=utf8mb4 -u root -e "source sql/seed-data.sql"
```

> Scriptul `seed-data.sql` adaugă date demonstrative și nu trebuie rulat la
> fiecare pornire. Rularea repetată poate produce erori de chei duplicate.

### 3. Configurează accesul la baza de date

Configurația implicită a backendului este:

```properties
db.url=jdbc:mariadb://localhost:3306/crm_training?createDatabaseIfNotExist=true
db.username=root
db.password=
```

Dacă serverul tău folosește alt utilizator, altă parolă sau alt port, adaugă
valorile corespunzătoare în
`src/main/resources/application.properties`. Nu publica parola bazei de date și
nu o include într-un commit.

### 4. Configurează funcțiile AI — opțional

În rădăcina proiectului, fișierul `.env` poate conține:

```dotenv
ANTHROPIC_API_KEY=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
```

Fișierul `.env` este ignorat de Git. Dacă lipsește sau cheia este goală,
aplicația pornește normal, însă chatbotul, traducerea și recomandările AI nu vor
fi disponibile.

### 5. Pornește backendul

Deschide primul terminal PowerShell în rădăcina proiectului:

```powershell
.\run-backend.ps1
```

Scriptul încarcă automat variabilele din `.env`, apoi pornește Spring Boot.
Backendul este pregătit când terminalul afișează un mesaj similar cu:

```text
Tomcat started on port 8080
Started CrmWebApplication
```

Dacă politica PowerShell blochează scriptul, rulează:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-backend.ps1
```

Alternativ, fără scriptul PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Pentru această variantă, cheia AI trebuie definită în terminal înainte de
pornire:

```powershell
$env:ANTHROPIC_API_KEY="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"
.\mvnw.cmd spring-boot:run
```

### 6. Pornește frontendul

Lasă backendul pornit și deschide un al doilea terminal:

```powershell
cd D:\TrainingIT_site\frontend
npm.cmd install
npm.cmd run dev
```

Pe sistemele unde scripturile PowerShell pentru npm sunt permise, comenzile pot
fi scrise și ca `npm install` și `npm run dev`.

### 7. Deschide aplicația

- Interfața web: [http://localhost:3000](http://localhost:3000)
- API backend: [http://localhost:8080/api](http://localhost:8080/api)
- Catalog public API:
  [http://localhost:8080/api/public/courses](http://localhost:8080/api/public/courses)
- Starea funcțiilor AI:
  [http://localhost:8080/api/ai/status](http://localhost:8080/api/ai/status)

## Pornire pe macOS sau Linux

Din rădăcina proiectului:

```bash
# Inițializare unică a bazei de date
mysql --default-character-set=utf8mb4 -u root -p < sql/schema.sql
mysql --default-character-set=utf8mb4 -u root -p < sql/seed-data.sql

# Cheia AI este opțională
export ANTHROPIC_API_KEY="cheia_ta_anthropic"

# Pornire backend
chmod +x mvnw
./mvnw spring-boot:run
```

Într-un al doilea terminal:

```bash
cd frontend
npm install
npm run dev
```

Accesează apoi [http://localhost:3000](http://localhost:3000).

## Conturi demonstrative

La prima pornire a backendului, conturile existente din tabelele `admins`,
`contacts` și `employees` primesc parola demonstrativă implicită `1234`.

Exemple de conturi admin create de datele inițiale:

- `andreibirceanu@adminit.ro`
- `costachemazarescu@adminit.ro`
- `pablovarga@adminit.ro`

Autentificarea unui administrator se poate face, de exemplu, cu:

```text
Email: andreibirceanu@adminit.ro
Parolă: 1234
```

Exemple de conturi din tabela `contacts`:

| Nume / companie | Tip contact | Email | Parolă |
| --- | --- | --- | --- |
| Alexandru Stoica | Persoană fizică | `alex.stoica@example.com` | `1234` |
| Cristina Marin | Persoană fizică | `cristina.marin@example.com` | `1234` |
| Bogdan Tudor | Persoană fizică | `bogdan.tudor@example.com` | `1234` |
| BankTech Solutions SRL | Companie | `hr@banktech.ro` | `1234` |

Aceste contacte sunt adăugate de `sql/seed-data.sql`. Parola `1234` este
atribuită automat la prima pornire a backendului.

Exemple de conturi din tabela `employees`:

| Nume | Companie | Email | Parolă |
| --- | --- | --- | --- |
| Andrei Popescu | SensiDEV | `andreipopescu@sensidev.ro` | `1234` |
| Maria Ionescu | SensiDEV | `mariaionescu@sensidev.ro` | `1234` |
| Gabriel Toma | Dedeman | `gabrieltoma@dedeman.ro` | `1234` |
| Ștefan Cojocaru | Antibiotice | `stefancojocaru@antibiotice.ro` | `1234` |

Conturile de angajați primesc automat reducerea de angajat de **60%**.
Exemplele de mai sus există în baza demonstrativă locală. La inițializarea unei
baze complet noi, angajații trebuie adăugați sau importați din zona
**Admin → Employees** înainte ca aceste adrese să poată fi folosite pentru
autentificare. Orice angajat nou primește implicit parola `1234`.

Aceste credențiale sunt exclusiv pentru dezvoltare și demonstrații. Nu folosi
parola `1234` într-un mediu public sau de producție.

Un vizitator își poate crea propriul cont din pagina **Înregistrare**. Conturile
create astfel primesc rolul de utilizator, nu rolul de administrator.

## Configurare

### Backend

Fișier principal:
`src/main/resources/application.properties`

Setările importante sunt:

| Setare | Valoare implicită | Rol |
| --- | --- | --- |
| `db.url` | `jdbc:mariadb://localhost:3306/crm_training?...` | Conexiunea la baza de date |
| `db.username` | `root` | Utilizatorul bazei de date |
| `db.password` | gol | Parola bazei de date |
| `crm.cors.allowed-origins` | `http://localhost:3000` | Originea frontendului permisă de CORS |
| `crm.ai.api-key` | variabila `ANTHROPIC_API_KEY` | Activează integrarea AI |
| `crm.ai.model` | `claude-opus-4-8` | Modelul Anthropic folosit |
| `crm.ai.max-tokens` | `2048` | Limita răspunsului AI |

Portul Spring Boot este implicit `8080`. Pentru a-l schimba, adaugă:

```properties
server.port=8081
```

### Frontend

Frontendul folosește implicit API-ul de la `http://localhost:8080`. Pentru altă
adresă, creează `frontend/.env.local`:

```dotenv
NEXT_PUBLIC_API_URL=http://localhost:8081
```

După modificarea variabilelor frontendului, repornește serverul Next.js.

Dacă schimbi portul frontendului sau rulezi aplicația pe alt domeniu,
actualizează și `crm.cors.allowed-origins` în backend.

## Verificarea aplicației

Cu baza de date și backendul pornite, verifică din PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/public/courses
Invoke-RestMethod http://localhost:8080/api/ai/status
```

Sau cu `curl`:

```bash
curl http://localhost:8080/api/public/courses
curl http://localhost:8080/api/ai/status
```

Checklist pentru pornirea completă:

- MariaDB/MySQL rulează pe portul `3306`.
- Baza `crm_training` conține schema și datele demonstrative.
- Backendul răspunde pe `http://localhost:8080`.
- Frontendul răspunde pe `http://localhost:3000`.
- Catalogul afișează cursurile.
- Autentificarea cu un cont demonstrativ funcționează.
- Endpointul AI raportează funcțiile active numai dacă cheia Anthropic este
  configurată.

## Build și teste

### Backend

Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd compile
```

macOS/Linux:

```bash
./mvnw test
./mvnw compile
```

### Frontend

```bash
cd frontend
npm run lint
npm run build
npm run start
```

`npm run start` pornește versiunea de producție locală și trebuie executat numai
după un `npm run build`.

## Oprirea aplicației

În fiecare dintre cele două terminale, apasă `Ctrl+C`:

1. oprește serverul Next.js;
2. oprește backendul Spring Boot;
3. oprește MariaDB/MySQL din XAMPP sau din managerul de servicii, dacă nu mai
   este folosit de altă aplicație.

Datele din baza de date rămân salvate și nu trebuie reimportate la următoarea
pornire.

La pornirile următoare sunt necesari doar pașii:

1. pornește MariaDB/MySQL;
2. rulează `.\run-backend.ps1`;
3. rulează `npm.cmd run dev` din directorul `frontend`;
4. deschide `http://localhost:3000`.

## Depanare

### Backendul nu se conectează la baza de date

Mesaje frecvente:

- `Connection refused` — serverul MariaDB/MySQL nu este pornit sau portul este
  greșit.
- `Access denied for user` — utilizatorul ori parola din
  `application.properties` nu corespund serverului.
- `Unknown database 'crm_training'` — scriptul `sql/schema.sql` nu a fost rulat.
- `Table ... doesn't exist` — schema nu a fost importată complet sau scripturile
  au fost executate în ordinea greșită.

### Portul 3000 sau 8080 este deja ocupat

În PowerShell:

```powershell
Get-NetTCPConnection -State Listen |
  Where-Object LocalPort -in 3000,8080 |
  Select-Object LocalAddress,LocalPort,OwningProcess
```

Oprește procesul vechi sau schimbă portul serviciului. Dacă schimbi portul
backendului, actualizează și `NEXT_PUBLIC_API_URL`.

### `npm.ps1 cannot be loaded because running scripts is disabled`

Folosește executabilul Windows:

```powershell
npm.cmd install
npm.cmd run dev
```

### Maven Wrapper nu pornește

Verifică mai întâi:

```powershell
java -version
.\mvnw.cmd -version
```

Dacă wrapperul nu poate descărca Maven, verifică accesul la internet, proxy-ul
și firewall-ul. Ca alternativă, instalează Maven 3.9+ și rulează:

```powershell
mvn spring-boot:run
```

### Frontendul afișează erori de rețea sau CORS

Verifică simultan:

- backendul rulează pe adresa definită în `NEXT_PUBLIC_API_URL`;
- `crm.cors.allowed-origins` conține adresa exactă a frontendului;
- ambele servere folosesc același protocol, de regulă `http` în dezvoltare.

### Funcțiile AI sunt indisponibile

Verifică:

```powershell
Invoke-RestMethod http://localhost:8080/api/ai/status
```

Apoi confirmă că `.env` se află în rădăcina proiectului, conține
`ANTHROPIC_API_KEY`, iar backendul a fost pornit prin `run-backend.ps1`.
Repornește backendul după orice modificare a cheii.

### Unde sunt logurile

Backendul scrie logurile aplicației în directorul `logs/`, în special în:

- `logs/crm-application.log`
- `logs/backend-run.log`, când pornirea este redirecționată către acest fișier

## Structura proiectului

```text
TrainingIT_site/
├── frontend/                       # Aplicația Next.js
│   ├── public/                     # Imagini și resurse statice
│   ├── src/app/                    # Pagini și rute
│   ├── src/components/             # Componente React
│   └── src/lib/                    # Client API și utilitare
├── src/main/java/crm/              # Backend Java și API REST
├── src/main/resources/
│   ├── application.properties      # Configurația backendului
│   └── logback.xml                 # Configurația logurilor
├── sql/
│   ├── schema.sql                  # Schema inițială a bazei de date
│   └── seed-data.sql               # Date demonstrative
├── docs/                           # Diagrame și documentație tehnică
├── logs/                           # Loguri locale
├── .env                            # Cheia AI locală; ignorată de Git
├── pom.xml                         # Configurația Maven
├── mvnw / mvnw.cmd                 # Maven Wrapper
└── run-backend.ps1                 # Pornirea backendului pe Windows
```

Diagramele de arhitectură și ale fluxurilor aplicației sunt descrise și în
`docs/application-diagrams.md`.
