const fs = require("fs");
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  AlignmentType, LevelFormat, HeadingLevel, BorderStyle, WidthType,
  ShadingType, VerticalAlign, PageNumber, PageBreak, TableOfContents,
  Header, Footer
} = require("docx");

// ---------- Paleta de culori ----------
const C = {
  brand:    "1F4E79", // albastru închis (titluri)
  brand2:   "2E75B6", // albastru mediu
  light:    "D6E4F0", // albastru deschis (fundal header tabel)
  ai:       "7030A0", // mov (AI)
  aiLight:  "E5D9F2",
  main:     "548235", // verde (tabele principale)
  mainLight:"C6E0B4",
  sec:      "BF8F00", // chihlimbar (tabele secundare)
  secLight: "FFE699",
  grey:     "808080",
  greyLight:"F2F2F2",
  white:    "FFFFFF",
  arrow:    "C00000",
  dataLayer:"FCE4D6",
  webLayer: "DEEBF7",
  svcLayer: "E2EFDA",
  dbLayer:  "FFF2CC",
  feLayer:  "D9E1F2",
};

const thin = (color) => ({ style: BorderStyle.SINGLE, size: 4, color });
const allBorders = (color = "B0B0B0") => ({
  top: thin(color), bottom: thin(color), left: thin(color), right: thin(color),
});
const noBorders = {
  top: { style: BorderStyle.NONE }, bottom: { style: BorderStyle.NONE },
  left: { style: BorderStyle.NONE }, right: { style: BorderStyle.NONE },
};
const CELLM = { top: 60, bottom: 60, left: 110, right: 110 };

// ---------- Helpers ----------
function run(text, opts = {}) { return new TextRun({ text, ...opts }); }

function p(text, opts = {}) {
  const { spacingAfter = 120, spacingBefore = 0, ...runOpts } = opts;
  return new Paragraph({
    spacing: { after: spacingAfter, before: spacingBefore },
    children: Array.isArray(text) ? text : [run(text, runOpts)],
  });
}

function bullet(text, opts = {}) {
  return new Paragraph({
    numbering: { reference: "bullets", level: opts.level || 0 },
    spacing: { after: 60 },
    children: Array.isArray(text) ? text : [run(text, opts)],
  });
}

function numbered(text, ref = "nums") {
  return new Paragraph({
    numbering: { reference: ref, level: 0 },
    spacing: { after: 60 },
    children: Array.isArray(text) ? text : [run(text)],
  });
}

function h1(text, brk = false) { return new Paragraph({ heading: HeadingLevel.HEADING_1, pageBreakBefore: brk, children: [run(text)] }); }
function h2(text) { return new Paragraph({ heading: HeadingLevel.HEADING_2, children: [run(text)] }); }
function h3(text) { return new Paragraph({ heading: HeadingLevel.HEADING_3, children: [run(text)] }); }

function cell(content, { fill, width, bold, color, align, valign, colSpan, font, size, borders } = {}) {
  const children = Array.isArray(content)
    ? content
    : [new Paragraph({
        alignment: align || AlignmentType.LEFT,
        children: [run(String(content), { bold: !!bold, color: color || "000000", font, size })],
      })];
  return new TableCell({
    width: width ? { size: width, type: WidthType.DXA } : undefined,
    columnSpan: colSpan,
    shading: fill ? { fill, type: ShadingType.CLEAR } : undefined,
    margins: CELLM,
    verticalAlign: valign || VerticalAlign.CENTER,
    borders: borders || allBorders(),
    children,
  });
}

// full-width colored band used for diagrams
function band(label, sub, fill, txtColor = "FFFFFF", width = 9360) {
  const kids = [new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: sub ? 20 : 0 },
    children: [run(label, { bold: true, color: txtColor, size: 22 })],
  })];
  if (sub) kids.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [run(sub, { color: txtColor, size: 18 })],
  }));
  return new Table({
    width: { size: width, type: WidthType.DXA },
    columnWidths: [width],
    rows: [new TableRow({ children: [new TableCell({
      width: { size: width, type: WidthType.DXA },
      shading: { fill, type: ShadingType.CLEAR },
      margins: { top: 100, bottom: 100, left: 120, right: 120 },
      borders: allBorders(fill),
      children: kids,
    })] })],
  });
}

function arrow(label) {
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 40, after: 40 },
    children: [run("▼", { color: C.arrow, bold: true, size: 20 }),
               ...(label ? [run("  " + label, { color: C.grey, italics: true, size: 16 })] : [])],
  });
}

// header row helper
function headerRow(labels, widths, fill = C.brand2) {
  return new TableRow({
    tableHeader: true,
    children: labels.map((l, i) => cell(l, { fill, width: widths[i], bold: true, color: "FFFFFF" })),
  });
}

function dataTable(widths, headerLabels, rows, headerFill) {
  const total = widths.reduce((a, b) => a + b, 0);
  return new Table({
    width: { size: total, type: WidthType.DXA },
    columnWidths: widths,
    rows: [
      headerRow(headerLabels, widths, headerFill),
      ...rows.map((r, ri) => new TableRow({
        children: r.map((c, i) => {
          const opts = (c && typeof c === "object" && "text" in c) ? c : { text: c };
          return cell(opts.text, {
            width: widths[i],
            fill: opts.fill || (ri % 2 ? C.greyLight : C.white),
            bold: opts.bold,
            color: opts.color,
            align: opts.align,
          });
        }),
      })),
    ],
  });
}

const CW = 9360; // content width US Letter, 1" margins

// =========================================================
// CONTENT
// =========================================================
const children = [];

// ---------- Pagina de titlu ----------
children.push(
  new Paragraph({ spacing: { before: 1800, after: 0 }, alignment: AlignmentType.CENTER,
    children: [run("JURNAL DETALIAT DE PROIECT", { bold: true, size: 56, color: C.brand })] }),
  new Paragraph({ spacing: { before: 120, after: 0 }, alignment: AlignmentType.CENTER,
    children: [run("CRM Training IT", { bold: true, size: 40, color: C.brand2 })] }),
  new Paragraph({ spacing: { before: 60, after: 600 }, alignment: AlignmentType.CENTER,
    children: [run("Aplicație Web pentru managementul relațiilor cu clienții, cu funcții de Inteligență Artificială",
      { italics: true, size: 24, color: C.grey })] }),
);
// title divider
children.push(new Paragraph({
  border: { bottom: { style: BorderStyle.SINGLE, size: 12, color: C.brand2, space: 1 } },
  spacing: { after: 300 }, children: [run("")],
}));
children.push(
  new Table({
    width: { size: 7000, type: WidthType.DXA }, columnWidths: [3000, 4000],
    alignment: AlignmentType.CENTER,
    rows: [
      ["Tip aplicație", "Web Application (Single Page App + REST API)"],
      ["Backend", "Java 17 + Spring Boot, JDBC, HikariCP"],
      ["Frontend", "Next.js 16 + React 19 + TypeScript + Tailwind CSS"],
      ["Bază de date", "MariaDB / MySQL (InnoDB)"],
      ["Inteligență Artificială", "Anthropic Claude API"],
      ["Autor", "Raul Veca"],
      ["Data", "25 iunie 2026"],
    ].map(([k, v], i) => new TableRow({ children: [
      cell(k, { width: 3000, fill: C.brand, color: "FFFFFF", bold: true }),
      cell(v, { width: 4000, fill: i % 2 ? C.greyLight : C.white }),
    ] })),
  }),
);
children.push(new Paragraph({ children: [new PageBreak()] }));

// ---------- Cuprins ----------
children.push(h1("Cuprins"));
children.push(new TableOfContents("Cuprins", { hyperlink: true, headingStyleRange: "1-2" }));
children.push(new Paragraph({ children: [new PageBreak()] }));

// =========================================================
// 1. Prezentare generală
// =========================================================
children.push(h1("1. Prezentare generală a proiectului"));
children.push(p([
  run("Proiectul ", {}),
  run("CRM Training IT", { bold: true }),
  run(" este o aplicație de tip CRM (Customer Relationship Management) dedicată unei firme de training IT. " +
      "Scopul aplicației este gestionarea întregului ciclu de vânzare și livrare a cursurilor: de la atragerea " +
      "potențialilor clienți (lead-uri), la calificarea lor, negocierea contractelor corporate, înscrierea la " +
      "sesiuni de curs și emiterea facturilor.", {}),
]));
children.push(p([
  run("Punct esențial al evoluției proiectului: ", { bold: true }),
  run("aplicația a fost transformată dintr-o aplicație Desktop (Java Swing) într-o aplicație Web", { bold: true, color: C.brand }),
  run(", conform cerinței obligatorii. Logica de business existentă (servicii, șabloane de proiectare, acces la date) " +
      "a fost păstrată și expusă printr-un API REST construit cu Spring Boot, peste care s-a adăugat o interfață web " +
      "modernă realizată în Next.js / React.", {}),
]));
children.push(h2("1.1 Cele două lumi ale aplicației"));
children.push(bullet([run("Zona publică (site vitrină): ", { bold: true }), run("vizitatorii și potențialii clienți individuali pot vedea companiile, obiectul lor de activitate și cursurile de care sunt interesate.")]));
children.push(bullet([run("Zona de administrare (CRM): ", { bold: true }), run("echipa de vânzări gestionează contacte, pipeline de oportunități, licitații, cursuri, angajați și funcțiile AI.")]));

// =========================================================
// 2. Cerințe obligatorii
// =========================================================
children.push(h1("2. Cerințele obligatorii și stadiul lor", true));
children.push(p("Tabelul de mai jos preia cerințele obligatorii din documentul „Targeturi obligatorii pentru proiect” și indică modul în care sunt acoperite în implementare. Verde = realizat, chihlimbar = parțial / în lucru."));
children.push(dataTable(
  [520, 5340, 1500, 2000],
  ["#", "Cerință obligatorie", "Stadiu", "Componenta din proiect"],
  [
    ["1", "Site public unde clienții individuali văd obiectul de activitate al companiilor și ce tip de cursuri îi interesează (inclusiv pentru investitori).", { text: "Realizat", fill: C.mainLight, bold: true }, "PublicCatalogController, /companies"],
    ["2", "Companiile aflate în etapa NEGOTIATION fac bidding (licitație) pentru un curs; cine oferă suma cea mai mare câștigă cursul.", { text: "Realizat", fill: C.mainLight, bold: true }, "Auction, Bid, AuctionService"],
    ["3", "Recomandări de cursuri pe baza profilului companiei (ML / AI / IT) și al angajaților, cu model AI.", { text: "Realizat", fill: C.mainLight, bold: true }, "AiService.recommendCoursesForCompany"],
    ["4", "Date detaliate despre angajați (profilul în care lucrează, profilele de interes), nu doar numărul lor.", { text: "Realizat", fill: C.mainLight, bold: true }, "Employee (work_profile, interest_profiles)"],
    ["5", "Chatbot.", { text: "Realizat", fill: C.mainLight, bold: true }, "Chatbot.tsx, AiController"],
    ["6", "API de AI (Claude / Gemini) folosit pentru vânzări.", { text: "Realizat", fill: C.mainLight, bold: true }, "ClaudeClient (Anthropic API)"],
    ["7", "Proiectul trebuie să fie Web Application, nu Desktop Application.", { text: "Realizat", fill: C.mainLight, bold: true }, "Spring Boot REST + Next.js"],
    ["8", "Neapărat interfață pentru aplicația web.", { text: "Realizat", fill: C.mainLight, bold: true }, "Frontend Next.js / React / Tailwind"],
  ],
  C.brand2,
));

// =========================================================
// 3. Arhitectura aplicației
// =========================================================
children.push(h1("3. Arhitectura aplicației (diagramă pe straturi)", true));
children.push(p("Aplicația respectă o arhitectură stratificată (layered architecture). Fiecare strat comunică doar cu stratul imediat inferior, ceea ce face codul ușor de întreținut și de testat. Săgețile roșii indică direcția fluxului unei cereri."));

children.push(band("STRAT PREZENTARE — Frontend", "Next.js 16 · React 19 · TypeScript · Tailwind CSS  (rulează în browser)", C.brand2));
children.push(arrow("HTTP / JSON  (fetch către /api/*)"));
children.push(band("STRAT WEB / API — Controllers REST", "Spring Boot · @RestController · /api/contacts, /api/opportunities, /api/ai, /api/auctions …", C.feLayer === C.feLayer ? "4472C4" : "4472C4"));
children.push(arrow("apel de metodă"));
children.push(band("STRAT FAȚADĂ + SERVICII (logica de business)", "CrmFacade · ContactService · OpportunityService · EnrollmentService · AuctionService · AiService", C.main));
children.push(arrow("apel de metodă"));
children.push(band("STRAT ACCES LA DATE — Repository + DAO", "AbstractRepository · ContactDao · OpportunityDao … (JDBC pur)", C.sec));
children.push(arrow("SQL prin HikariCP (pool de conexiuni)"));
children.push(band("BAZA DE DATE — MariaDB / MySQL", "17 tabele · motor InnoDB · chei străine (FK) între entități", C.brand));

children.push(new Paragraph({ spacing: { before: 200 }, children: [] }));
children.push(p([run("Ramură transversală — Inteligență Artificială: ", { bold: true, color: C.ai }),
  run("stratul de servicii apelează ")]));
children.push(band("AiService  →  ClaudeClient  →  Anthropic Claude API", "Recomandări de cursuri · Asistent de vânzări · Chatbot", C.ai));

children.push(h2("3.1 Stack tehnologic"));
children.push(dataTable(
  [2600, 6760],
  ["Tehnologie", "Rol în proiect"],
  [
    ["Java 17", "Limbajul de bază al backend-ului și al logicii de business."],
    ["Spring Boot (Web, Validation)", "Server HTTP, expunerea API-ului REST, injecția de dependențe, validări."],
    ["JDBC + HikariCP", "Acces la baza de date prin SQL și pool de conexiuni performant."],
    ["MariaDB / MySQL", "Persistența datelor în tabele relaționale (InnoDB)."],
    ["Lombok", "Reduce codul boilerplate (getteri, setteri, builderi) din entități."],
    ["SLF4J + Logback", "Sistemul de logare al aplicației."],
    ["anthropic-java SDK", "Clientul oficial pentru apelarea modelului Claude (funcțiile AI)."],
    ["Next.js 16 / React 19", "Interfața web (SPA), randare și rutare pe partea de client."],
    ["TypeScript + Tailwind CSS", "Tipare statică și stilizarea rapidă a interfeței."],
  ],
  C.brand2,
));

// =========================================================
// 4. Structura proiectului
// =========================================================
children.push(h1("4. Structura proiectului (organizarea pe pachete)", true));
children.push(p("Codul sursă este organizat pe pachete (foldere) după responsabilitate. Arborele de mai jos rezumă pachetele importante din backend și folderul frontend."));

const tree = [
  ["crm/", "rădăcina aplicației Java", 0, C.brand],
  ["model/entity/", "13 entități de domeniu (Contact, Course, Opportunity, Auction, Bid …)", 1, C.main],
  ["model/enums/", "14 enumerări (LeadStatus, OpportunityStage, ProfileArea …)", 1, C.main],
  ["dao/", "acces la date prin JDBC (AbstractDao + 11 DAO-uri)", 1, C.sec],
  ["repository/", "repository peste DAO (AbstractRepository + repos)", 1, C.sec],
  ["service/", "logica de business (Contact, Opportunity, Enrollment, Auction, Employee …)", 1, C.brand2],
  ["facade/", "CrmFacade — punct unic de intrare către servicii", 1, C.brand2],
  ["builder/ command/ factory/ observer/ strategy/ validation/", "șabloane de proiectare", 1, C.ai],
  ["web/controller/", "11 controllere REST (/api/*)", 1, "4472C4"],
  ["web/ai/", "AiService, ClaudeClient, DTO-uri AI", 1, C.ai],
  ["web/config/", "configurare web, inițializare schemă (employees, auctions, bids)", 1, "4472C4"],
  ["gui/", "interfața Swing moștenită (Desktop — istoric)", 1, C.grey],
  ["frontend/src/", "aplicația Next.js (pagini admin, companies, Chatbot, api.ts)", 0, C.brand2],
];
children.push(new Table({
  width: { size: CW, type: WidthType.DXA }, columnWidths: [3400, 5960],
  rows: tree.map(([name, desc, indent, color]) => new TableRow({ children: [
    new TableCell({ width: { size: 3400, type: WidthType.DXA }, margins: CELLM, borders: allBorders("D9D9D9"),
      children: [new Paragraph({ children: [run((indent ? "   └─ " : "") + name, { font: "Consolas", bold: indent === 0, color })] })] }),
    cell(desc, { width: 5960, fill: C.white }),
  ] })),
}));

// =========================================================
// 5. Design patterns
// =========================================================
children.push(h1("5. Șabloane de proiectare (Design Patterns)", true));
children.push(p("Proiectul folosește numeroase șabloane de proiectare clasice (Gang of Four), ceea ce demonstrează o arhitectură matură. Tabelul leagă fiecare șablon de clasele care îl implementează și de rolul lui."));
children.push(dataTable(
  [2100, 3500, 3760],
  ["Șablon", "Clase reprezentative", "Rol în aplicație"],
  [
    ["Singleton", "DatabaseConnection", "O singură instanță a pool-ului de conexiuni la baza de date."],
    ["Builder", "ContactBuilder, OpportunityBuilder", "Construirea pas cu pas a obiectelor complexe."],
    ["Factory Method", "NotificationFactory, Email/SmsNotificationFactory, ActivityFactory", "Crearea de notificări și activități fără a cupla clientul de tipul concret."],
    ["Command", "AbstractCommand, CommandInvoker, CreateContactCommand …", "Încapsularea acțiunilor (creare contact, mutare etapă) ca obiecte."],
    ["Observer", "EventBus, CrmEvent, WelcomeEmailObserver, LeadScoreUpdateObserver, AuditLogObserver", "Reacții automate la evenimente (ex.: trimitere email de bun venit)."],
    ["Strategy", "PricingStrategy (Individual/Corporate), LeadScoringStrategy (B2B/Simple)", "Algoritmi interschimbabili pentru prețuri și scoring de lead-uri."],
    ["Facade", "CrmFacade", "Interfață simplificată peste multitudinea de servicii."],
    ["Repository + DAO", "AbstractRepository, AbstractDao + implementări", "Separarea logicii de business de accesul efectiv la date."],
    ["Chain of Responsibility", "ContactValidatorChain (Email, Phone, Gdpr, ContactType)", "Înlănțuirea validatorilor de contacte."],
  ],
  C.ai,
));

// =========================================================
// 6. Clasificarea tabelelor
// =========================================================
children.push(h1("6. Modelul de date — clasificarea tabelelor", true));
children.push(p([
  run("Baza de date conține ", {}), run("17 tabele", { bold: true }),
  run(". Le împărțim în ", {}),
  run("tabele principale", { bold: true, color: C.main }),
  run(" (entitățile centrale, pe care se construiește aplicația) și ", {}),
  run("tabele secundare", { bold: true, color: C.sec }),
  run(" (de suport: documente financiare, jurnale, evenimente), care depind de cele principale.", {}),
]));

children.push(h2("6.1 Tabele PRINCIPALE"));
children.push(p("Sunt entitățile-nucleu, multe dintre ele „hub-uri” spre care converg cheile străine ale altor tabele."));
children.push(dataTable(
  [2100, 3000, 4260],
  ["Tabel", "Reprezintă", "De ce este principal"],
  [
    [{ text: "users", bold: true }, "Utilizatorii sistemului (agenți de vânzări, admini, traineri).", "Hub: contacte, oportunități, activități, facturi îi referă prin assigned_to / created_by."],
    [{ text: "contacts", bold: true }, "Lead-uri și clienți, atât persoane (B2C) cât și companii (B2B).", "Entitatea centrală a unui CRM; referită de aproape toate celelalte tabele."],
    [{ text: "courses", bold: true }, "Catalogul de cursuri IT oferite.", "Stă la baza sesiunilor, înscrierilor și licitațiilor."],
    [{ text: "trainers", bold: true }, "Trainerii care livrează cursurile.", "Resursă-cheie alocată sesiunilor de curs."],
    [{ text: "course_sessions", bold: true }, "Instanțe concrete (date, locație) ale unui curs.", "Leagă cursul de trainer și de participanți (înscrieri)."],
    [{ text: "enrollments", bold: true }, "Înscrierea unui contact la o sesiune de curs.", "Tabel de legătură (N:M) între contacte și sesiuni; sursă de venit."],
    [{ text: "opportunities", bold: true }, "Oportunități de vânzare B2B (pipeline).", "Hub pentru propuneri, contracte, activități și licitații."],
    [{ text: "employees", bold: true }, "Angajații companiilor-client (profil de lucru și interese).", "Date necesare recomandărilor AI (cerința #4)."],
    [{ text: "auctions", bold: true }, "Licitații în care companiile licitează pentru un curs.", "Funcționalitate de bidding (cerința #2)."],
    [{ text: "bids", bold: true }, "Ofertele (sumele) depuse de companii într-o licitație.", "Determină câștigătorul licitației."],
  ],
  C.main,
));

children.push(h2("6.2 Tabele SECUNDARE (de suport)"));
children.push(p("Depind de tabelele principale și acoperă zona financiară, de monitorizare și de evenimente."));
children.push(dataTable(
  [2100, 3000, 4260],
  ["Tabel", "Reprezintă", "Depinde de"],
  [
    [{ text: "proposals", bold: true }, "Propuneri comerciale pentru o oportunitate.", "opportunities, users"],
    [{ text: "contracts", bold: true }, "Contracte semnate cu clientul.", "opportunities, contacts"],
    [{ text: "invoices", bold: true }, "Facturi emise.", "contracts, enrollments, contacts"],
    [{ text: "payments", bold: true }, "Plăți încasate pe facturi / înscrieri.", "invoices, enrollments, users"],
    [{ text: "activities", bold: true }, "Interacțiuni (apeluri, întâlniri, e-mailuri).", "contacts, opportunities, users"],
    [{ text: "lead_score_logs", bold: true }, "Istoricul modificărilor scorului de lead.", "contacts"],
    [{ text: "audit_logs", bold: true }, "Jurnal de audit al modificărilor din sistem.", "users"],
  ],
  C.sec,
));

// legend
children.push(new Paragraph({ spacing: { before: 120, after: 80 }, children: [run("Legendă:", { bold: true })] }));
children.push(new Table({
  width: { size: 5000, type: WidthType.DXA }, columnWidths: [1400, 3600],
  rows: [
    new TableRow({ children: [cell("", { width: 1400, fill: C.mainLight }), cell("Tabel principal (entitate centrală)", { width: 3600, borders: noBorders })] }),
    new TableRow({ children: [cell("", { width: 1400, fill: C.secLight }), cell("Tabel secundar (de suport)", { width: 3600, borders: noBorders })] }),
  ],
}));

// =========================================================
// 7. Diagrama entitate-relație (ERD)
// =========================================================
children.push(h1("7. Diagrama entitate-relație (ERD)", true));
children.push(p("Diagrama de mai jos prezintă vizual entitățile și legăturile dintre ele. Casetele verzi sunt tabele principale, cele chihlimbar sunt secundare. Săgețile „→” se citesc „referă / aparține de”."));

// helper for an ERD box
function erdBox(name, fill, width) {
  return new TableCell({
    width: { size: width, type: WidthType.DXA }, margins: CELLM,
    shading: { fill, type: ShadingType.CLEAR }, verticalAlign: VerticalAlign.CENTER,
    borders: allBorders("7F7F7F"),
    children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [run(name, { bold: true, size: 18 })] })],
  });
}
function erdArrowRow(label, width = CW) {
  return new TableRow({ children: [new TableCell({ width: { size: width, type: WidthType.DXA }, borders: noBorders,
    children: [new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 20, after: 20 },
      children: [run("↓  " + label, { color: C.arrow, size: 16, italics: true })] })] })] });
}

// Build ERD as grouped clusters around hubs
children.push(h2("7.1 Hub: USERS"));
children.push(new Table({ width: { size: CW, type: WidthType.DXA }, columnWidths: [CW],
  rows: [
    new TableRow({ children: [new TableCell({ width: { size: CW, type: WidthType.DXA }, borders: noBorders,
      children: [ new Table({ width: { size: 3000, type: WidthType.DXA }, columnWidths: [3000], alignment: AlignmentType.CENTER,
        rows: [ new TableRow({ children: [ erdBox("USERS", C.mainLight, 3000) ] }) ] }) ] })] }),
    erdArrowRow("este referit (assigned_to / created_by) de:"),
    new TableRow({ children: [new TableCell({ width: { size: CW, type: WidthType.DXA }, borders: noBorders,
      children: [ new Table({ width: { size: CW, type: WidthType.DXA }, columnWidths: [1872,1872,1872,1872,1872],
        rows: [ new TableRow({ children: [
          erdBox("contacts", C.mainLight, 1872), erdBox("opportunities", C.mainLight, 1872),
          erdBox("activities", C.secLight, 1872), erdBox("proposals", C.secLight, 1872),
          erdBox("trainers", C.mainLight, 1872) ] }) ] }) ] })] }),
  ] }));

children.push(h2("7.2 Hub: CONTACTS (clienți / lead-uri)"));
children.push(new Table({ width: { size: CW, type: WidthType.DXA }, columnWidths: [CW],
  rows: [
    new TableRow({ children: [new TableCell({ width: { size: CW, type: WidthType.DXA }, borders: noBorders,
      children: [ new Table({ width: { size: 3000, type: WidthType.DXA }, columnWidths: [3000], alignment: AlignmentType.CENTER,
        rows: [ new TableRow({ children: [ erdBox("CONTACTS", C.mainLight, 3000) ] }) ] }) ] })] }),
    erdArrowRow("este referit de:"),
    new TableRow({ children: [new TableCell({ width: { size: CW, type: WidthType.DXA }, borders: noBorders,
      children: [ new Table({ width: { size: CW, type: WidthType.DXA }, columnWidths: [1560,1560,1560,1560,1560,1560],
        rows: [ new TableRow({ children: [
          erdBox("enrollments", C.mainLight, 1560), erdBox("opportunities", C.mainLight, 1560),
          erdBox("employees", C.mainLight, 1560), erdBox("contracts", C.secLight, 1560),
          erdBox("invoices", C.secLight, 1560), erdBox("activities", C.secLight, 1560) ] }) ] }) ] })] }),
  ] }));

children.push(h2("7.3 Lanțul COURSE → SESSION → ENROLLMENT"));
children.push(new Table({ width: { size: CW, type: WidthType.DXA }, columnWidths: [CW], rows: [
  new TableRow({ children: [ new TableCell({ width: { size: CW, type: WidthType.DXA }, borders: noBorders, children: [
    new Table({ width: { size: 7800, type: WidthType.DXA }, columnWidths: [2400,300,2400,300,2400], alignment: AlignmentType.CENTER, rows: [ new TableRow({ children: [
      erdBox("courses", C.mainLight, 2400),
      new TableCell({ width: { size: 300, type: WidthType.DXA }, borders: noBorders, verticalAlign: VerticalAlign.CENTER, children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [run("→", { bold: true, color: C.arrow })] })] }),
      erdBox("course_sessions", C.mainLight, 2400),
      new TableCell({ width: { size: 300, type: WidthType.DXA }, borders: noBorders, verticalAlign: VerticalAlign.CENTER, children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [run("→", { bold: true, color: C.arrow })] })] }),
      erdBox("enrollments", C.mainLight, 2400),
    ] }) ] }) ] }) ] }),
] }));
children.push(p([run("De asemenea: ", { italics: true }), run("trainers", { font: "Consolas" }), run(" → ", {}), run("course_sessions", { font: "Consolas" }), run("  și  ", {}), run("courses", { font: "Consolas" }), run(" → ", {}), run("auctions", { font: "Consolas" }), run(" → ", {}), run("bids", { font: "Consolas" }), run(".", {})]));

children.push(h2("7.4 Lanțul financiar (tabele secundare)"));
children.push(new Table({ width: { size: CW, type: WidthType.DXA }, columnWidths: [CW], rows: [
  new TableRow({ children: [ new TableCell({ width: { size: CW, type: WidthType.DXA }, borders: noBorders, children: [
    new Table({ width: { size: 8400, type: WidthType.DXA }, columnWidths: [2600,300,2600,300,2600], alignment: AlignmentType.CENTER, rows: [ new TableRow({ children: [
      erdBox("opportunities", C.mainLight, 2600),
      new TableCell({ width: { size: 300, type: WidthType.DXA }, borders: noBorders, verticalAlign: VerticalAlign.CENTER, children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [run("→", { bold: true, color: C.arrow })] })] }),
      erdBox("contracts", C.secLight, 2600),
      new TableCell({ width: { size: 300, type: WidthType.DXA }, borders: noBorders, verticalAlign: VerticalAlign.CENTER, children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [run("→", { bold: true, color: C.arrow })] })] }),
      erdBox("invoices", C.secLight, 2600),
    ] }) ] }) ] }) ] }),
  erdArrowRow("invoices ← payments (plăți pe factură)"),
] }));

// =========================================================
// 8. Tabel cu legaturile (FK / interogari SQL)
// =========================================================
children.push(h1("8. Tabel cu legăturile (chei străine / interogări SQL)", true));
children.push(p("Acesta este tabelul cerut explicit: legăturile dintre tabele de tipul „interogare SQL”. Fiecare rând descrie o cheie străină (FK): din ce tabel pleacă, prin ce coloană, către ce tabel ajunge, ce tip de relație reprezintă și un exemplu de JOIN."));
children.push(dataTable(
  [1900, 1700, 1600, 1400, 2760],
  ["Tabel sursă", "Coloană (FK)", "→ Tabel țintă", "Relație", "Exemplu de JOIN (SQL)"],
  [
    ["contacts", "assigned_to", "users", "N:1", "JOIN users u ON c.assigned_to = u.id"],
    ["trainers", "user_id", "users", "1:1", "JOIN users u ON t.user_id = u.id"],
    ["course_sessions", "course_id", "courses", "N:1", "JOIN courses c ON s.course_id = c.id"],
    ["course_sessions", "trainer_id", "trainers", "N:1", "JOIN trainers t ON s.trainer_id = t.id"],
    ["enrollments", "session_id", "course_sessions", "N:1", "JOIN course_sessions s ON e.session_id = s.id"],
    ["enrollments", "contact_id", "contacts", "N:1", "JOIN contacts c ON e.contact_id = c.id"],
    ["opportunities", "client_id", "contacts", "N:1", "JOIN contacts c ON o.client_id = c.id"],
    ["opportunities", "assigned_to", "users", "N:1", "JOIN users u ON o.assigned_to = u.id"],
    ["proposals", "opportunity_id", "opportunities", "N:1", "JOIN opportunities o ON p.opportunity_id = o.id"],
    ["proposals", "created_by", "users", "N:1", "JOIN users u ON p.created_by = u.id"],
    ["contracts", "opportunity_id", "opportunities", "N:1", "JOIN opportunities o ON k.opportunity_id = o.id"],
    ["contracts", "client_id", "contacts", "N:1", "JOIN contacts c ON k.client_id = c.id"],
    ["invoices", "contract_id", "contracts", "N:1", "JOIN contracts k ON i.contract_id = k.id"],
    ["invoices", "enrollment_id", "enrollments", "N:1", "JOIN enrollments e ON i.enrollment_id = e.id"],
    ["invoices", "client_id", "contacts", "N:1", "JOIN contacts c ON i.client_id = c.id"],
    ["payments", "invoice_id", "invoices", "N:1", "JOIN invoices i ON pm.invoice_id = i.id"],
    ["payments", "enrollment_id", "enrollments", "N:1", "JOIN enrollments e ON pm.enrollment_id = e.id"],
    ["payments", "created_by", "users", "N:1", "JOIN users u ON pm.created_by = u.id"],
    ["activities", "contact_id", "contacts", "N:1", "JOIN contacts c ON a.contact_id = c.id"],
    ["activities", "opportunity_id", "opportunities", "N:1", "JOIN opportunities o ON a.opportunity_id = o.id"],
    ["activities", "assigned_to", "users", "N:1", "JOIN users u ON a.assigned_to = u.id"],
    ["lead_score_logs", "contact_id", "contacts", "N:1", "JOIN contacts c ON l.contact_id = c.id"],
    ["audit_logs", "user_id", "users", "N:1", "JOIN users u ON al.user_id = u.id"],
    ["employees", "company_id", "contacts", "N:1", "JOIN contacts c ON em.company_id = c.id"],
    ["auctions", "course_id", "courses", "N:1", "JOIN courses c ON au.course_id = c.id"],
    ["auctions", "winner_company_id", "contacts", "N:1", "JOIN contacts c ON au.winner_company_id = c.id"],
    ["bids", "auction_id", "auctions", "N:1", "JOIN auctions au ON b.auction_id = au.id"],
    ["bids", "company_id", "contacts", "N:1", "JOIN contacts c ON b.company_id = c.id"],
  ].map(r => [{ text: r[0], font: "Consolas" }, { text: r[1], font: "Consolas" }, { text: r[2], font: "Consolas", bold: true }, { text: r[3], align: AlignmentType.CENTER }, { text: r[4], font: "Consolas", size: 16 }]),
  C.brand2,
));
children.push(p([run("Notă: ", { bold: true }), run("relațiile employees, auctions și bids către contacts/courses sunt logice (impuse de aplicație). Tabelele lor sunt create de WebSchemaInitializer cu indecși, fără constrângeri FK fizice, pentru a rămâne compatibile cu baza de date moștenită.", { italics: true })]));

children.push(h2("8.1 Exemplu de interogare completă (raport de înscrieri)"));
const sql = [
  "SELECT  c.first_name, c.last_name,",
  "        co.name        AS curs,",
  "        s.start_date,",
  "        t.last_name    AS trainer,",
  "        e.final_price, e.payment_status",
  "FROM        enrollments      e",
  "JOIN        course_sessions  s  ON e.session_id = s.id",
  "JOIN        courses          co ON s.course_id  = co.id",
  "JOIN        contacts         c  ON e.contact_id = c.id",
  "LEFT JOIN   trainers         t  ON s.trainer_id = t.id",
  "WHERE       e.status = 'CONFIRMED'",
  "ORDER BY    s.start_date;",
];
children.push(new Table({ width: { size: CW, type: WidthType.DXA }, columnWidths: [CW], rows: [
  new TableRow({ children: [ new TableCell({ width: { size: CW, type: WidthType.DXA }, shading: { fill: "1E1E1E", type: ShadingType.CLEAR }, margins: { top: 120, bottom: 120, left: 160, right: 160 }, borders: allBorders("1E1E1E"),
    children: sql.map(line => new Paragraph({ spacing: { after: 0 }, children: [run(line, { font: "Consolas", color: "D4D4D4", size: 18 })] })) }) ] }),
] }));

// =========================================================
// 9. Functionalitatile AI
// =========================================================
children.push(h1("9. Funcționalitățile de Inteligență Artificială", true));
children.push(p("Trei funcții AI sunt construite peste API-ul Claude (Anthropic), accesate prin clasa ClaudeClient. Serviciul AiService adună contextul de domeniu (companie, angajați, catalog) și construiește prompturile."));
children.push(dataTable(
  [2600, 6760],
  ["Funcție AI", "Descriere"],
  [
    [{ text: "Recomandări de cursuri", bold: true, color: C.ai }, "Pe baza profilului companiei și al angajaților (work_profile, interest_profiles), modelul returnează un JSON cu cursurile potrivite și un scor de potrivire 0–100 (cerințele #3 și #4)."],
    [{ text: "Asistent de vânzări", bold: true, color: C.ai }, "Sprijină echipa de vânzări cu sugestii și argumentare comercială (cerința #6)."],
    [{ text: "Chatbot", bold: true, color: C.ai }, "Componenta Chatbot.tsx din frontend conversează cu vizitatorii prin endpointul /api/ai (cerința #5)."],
  ],
  C.ai,
));
children.push(h2("9.1 Fluxul unei recomandări AI"));
children.push(band("Frontend (pagina companiei) — cere recomandări", null, C.brand2));
children.push(arrow("GET /api/ai/recommendations/{companyId}"));
children.push(band("AiController → AiService.recommendCoursesForCompany()", "adună: Contact (companie) + Employees + Catalog cursuri", C.main));
children.push(arrow("construiește prompt + context"));
children.push(band("ClaudeClient → Anthropic Claude API", "model AI generează lista de cursuri", C.ai));
children.push(arrow("răspuns JSON (courseId, courseName, reason, matchScore)"));
children.push(band("Frontend afișează cursurile recomandate", null, C.brand2));

// =========================================================
// 10. Licitatia
// =========================================================
children.push(h1("10. Modulul de licitație (Auction / Bidding)", true));
children.push(p([
  run("Cerința #2: companiile aflate în etapa ", {}),
  run("NEGOTIATION", { bold: true, color: C.brand }),
  run(" pot licita pentru un curs; cine oferă suma cea mai mare câștigă. Modulul folosește entitățile ", {}),
  run("Auction", { font: "Consolas" }), run(" și ", {}), run("Bid", { font: "Consolas" }),
  run(", coordonate de AuctionService și expuse prin /api/auctions.", {}),
]));
children.push(h2("10.1 Fluxul licitației"));
children.push(numbered("Administratorul deschide o licitație (auctions) pentru un curs, cu un preț de pornire și un termen (closes_at).", "nums2"));
children.push(numbered("Companiile eligibile (cu o oportunitate în etapa NEGOTIATION) depun oferte (bids) cu sume tot mai mari.", "nums2"));
children.push(numbered("La închidere, oferta cu suma cea mai mare este declarată câștigătoare; auctions.winner_company_id și winning_amount sunt completate.", "nums2"));
children.push(numbered("Compania câștigătoare obține dreptul la curs, iar oportunitatea poate avansa spre contract.", "nums2"));
children.push(dataTable(
  [2200, 7160],
  ["Câmp cheie", "Rol"],
  [
    ["auctions.status", "OPEN / CLOSED / AWARDED — starea licitației (enum AuctionStatus)."],
    ["auctions.starting_price", "Suma minimă de pornire."],
    ["bids.amount", "Suma oferită de o companie; cea mai mare câștigă."],
    ["auctions.winner_company_id", "Compania câștigătoare (referă contacts)."],
  ],
  C.brand2,
));

// =========================================================
// 11. API REST
// =========================================================
children.push(h1("11. API-ul REST (puncte de acces)", true));
children.push(p("Backend-ul expune logica printr-un set de controllere REST. Acestea sunt „interogările” pe care frontend-ul le folosește pentru a citi și modifica datele."));
children.push(dataTable(
  [2600, 3000, 3760],
  ["Endpoint de bază", "Controller", "Responsabilitate"],
  [
    ["/api/public", "PublicCatalogController", "Zona publică: companii și cursuri vizibile vizitatorilor (cerința #1)."],
    ["/api/contacts", "ContactController", "Lead-uri și clienți (B2C / B2B)."],
    ["/api/opportunities", "OpportunityController", "Pipeline-ul de oportunități B2B."],
    ["/api/auctions", "AuctionController", "Licitațiile și ofertele (cerința #2)."],
    ["/api/courses", "CourseController", "Catalogul de cursuri."],
    ["/api/employees", "EmployeeController", "Angajații companiilor (profil + interese)."],
    ["/api/enrollments", "EnrollmentController", "Înscrierile la sesiuni."],
    ["/api/activities", "ActivityController", "Activități / interacțiuni."],
    ["/api/ai", "AiController", "Recomandări, asistent de vânzări, chatbot (cerințele #3, #5, #6)."],
    ["/api/stats", "StatsController", "Statistici și indicatori."],
    ["/api/meta", "MetaController", "Date de configurare / enumerări pentru interfață."],
  ].map(r => [{ text: r[0], font: "Consolas", bold: true }, { text: r[1], font: "Consolas", size: 18 }, r[2]]),
  "4472C4",
));

// =========================================================
// 12. Jurnal cronologic
// =========================================================
children.push(h1("12. Jurnal cronologic de dezvoltare", true));
children.push(p("Etapele majore ale proiectului, reconstituite din istoricul de versiuni (git) și din structura codului."));
children.push(dataTable(
  [1700, 3000, 4660],
  ["Etapă", "Reper", "Ce s-a realizat"],
  [
    ["Etapa 1", "Proiect fără interfață", "Modelul de domeniu, accesul la date (DAO/Repository), serviciile și șabloanele de proiectare; rularea în consolă."],
    ["Etapa 2", "Interfața CRM Swing", "Interfață Desktop (Java Swing): contacte, pipeline, cursuri, activități, statistici."],
    ["Etapa 3", "Transformarea în Web", "Expunerea logicii printr-un API REST Spring Boot; păstrarea logicii de business existente."],
    ["Etapa 4", "Funcții web + AI", "Tabelele employees / auctions / bids; integrarea Claude pentru recomandări, chatbot și asistent de vânzări; frontend Next.js."],
  ],
  C.brand,
));
children.push(h2("12.1 Concluzii și pași următori"));
children.push(bullet("Toate cele 8 cerințe obligatorii sunt acoperite în implementarea actuală."));
children.push(bullet("Arhitectura stratificată și șabloanele de proiectare oferă o bază solidă pentru extindere."));
children.push(bullet([run("Pas următor recomandat: ", { bold: true }), run("aducerea tabelelor employees / auctions / bids în schema oficială cu constrângeri FK fizice și antrenarea/îmbunătățirea modelului de recomandare.")]));

// =========================================================
// DOCUMENT
// =========================================================
const doc = new Document({
  creator: "Raul Veca",
  title: "Jurnal detaliat de proiect — CRM Training IT",
  styles: {
    default: { document: { run: { font: "Calibri", size: 22 } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 32, bold: true, color: C.brand, font: "Calibri" },
        paragraph: { spacing: { before: 280, after: 160 }, outlineLevel: 0,
          border: { bottom: { style: BorderStyle.SINGLE, size: 8, color: C.brand2, space: 4 } } } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 26, bold: true, color: C.brand2, font: "Calibri" },
        paragraph: { spacing: { before: 200, after: 120 }, outlineLevel: 1 } },
      { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 23, bold: true, color: "404040", font: "Calibri" },
        paragraph: { spacing: { before: 140, after: 80 }, outlineLevel: 2 } },
    ],
  },
  numbering: {
    config: [
      { reference: "bullets", levels: [{ level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 600, hanging: 300 } } } }] },
      { reference: "nums", levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 600, hanging: 300 } } } }] },
      { reference: "nums2", levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 600, hanging: 300 } } } }] },
    ],
  },
  sections: [{
    properties: { page: { size: { width: 12240, height: 15840 }, margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } } },
    headers: { default: new Header({ children: [new Paragraph({
      alignment: AlignmentType.RIGHT,
      border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: "BFBFBF", space: 2 } },
      children: [run("CRM Training IT — Jurnal de proiect", { color: "808080", size: 16 })] })] }) },
    footers: { default: new Footer({ children: [new Paragraph({
      alignment: AlignmentType.CENTER,
      children: [run("Pagina ", { color: "808080", size: 16 }),
                 new TextRun({ children: [PageNumber.CURRENT], color: "808080", size: 16 }),
                 run(" / ", { color: "808080", size: 16 }),
                 new TextRun({ children: [PageNumber.TOTAL_PAGES], color: "808080", size: 16 })] })] }) },
    children,
  }],
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync("docs/Jurnal_Detaliat_Proiect_CRM.docx", buffer);
  console.log("DOCX scris: docs/Jurnal_Detaliat_Proiect_CRM.docx (" + buffer.length + " bytes)");
});
