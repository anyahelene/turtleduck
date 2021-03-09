# Kort oppsummering

* Vi bruker Cookies (informasjonskapsler) og Web Storage i nettleseren din.
* TurtleDuck er en *veldig eksperimentell* tjeneste:
   * Du kan oppleve avbrudd og datatap når som helst.
   * Vi logger mye for å kunne finne og fikse feil.
   * Vær grei – ikke misbruk tjenesten til skade eller sjenanse, eller for profitt eller
     ulovligheter.
  
# Bruk av Informasjonskapsler

Vi bruker informasjonskapsler for å ordne innloggingen. I
informasjonskapselen ligger en [sesjons-id](https://en.wikipedia.org/wiki/Session_ID) for innloggingen din
og et [sikkerhetstoken](https://en.wikipedia.org/wiki/Cross-site_request_forgery#Cookie-to-header%20token),
som begge er nødvendige for sikker tilgang til nettsiden. Vi deler ikke
denne informasjonen med noen.

For generell om informasjonskapsler se en av disse nettsidene:

* [*https://no.wikipedia.org/wiki/Informasjonskapsel*](https://no.wikipedia.org/wiki/Informasjonskapsel)

* [*https://en.wikipedia.org/wiki/HTTP\_cookie*](https://en.wikipedia.org/wiki/HTTP_cookie)

* [*https://www.datatilsynet.no/personvern-pa-ulike-omrader/internett-og-apper/cookies/*](https://www.datatilsynet.no/personvern-pa-ulike-omrader/internett-og-apper/cookies/)

# Datainnsamling

TurtleDuck er en eksperimentell tjeneste, og du får tilgang utelukkende for å
kunne hjelpe med testing, tilbakemeldinger og videreutvikling. Vi logger
aggressivt all interaksjon med nettsiden, særlig når det oppstår feil – dette
kan også inkludere feil som blir logget i JavaScript-konsollet i nettleseren
din (F12 i nettleseren). For øyeblikket er målet med all logging og
datainnsamling å avsløre feil og forbedre tjenesten – på et senere tidspunkt kan
det være aktuelt med datainnsamling for pedagogikk-/forskningsformål, som i
såfall vi skje kun etter informert samtykke fra brukerne, ved et egen
samtykkeskjema.

# Sikkerhet og bruk av virtuell datamaskin

Mens du er logget inn og aktiv i TurtleDuck har du tilgang til en egen liten
virtuell datamaskin ([«container»](https://en.wikipedia.org/wiki/OS-level_virtualization)
der koden du skriver blir kompilert og kjørt. Den virtuelle
maskinen har veldig begrenset tilgang og begrensede ressurser, men det kan
likevel være mulig å misbruke vår tillit ved å kjøre programkode som er til
skade eller sjenanse. Hvis vi oppdager at du misbruker tjenesten eller forleder
andre til å gjøre det kan du miste tilgang uten forvarsel. Lovstridig bruk vil
eventuelt bli politianmeldt.

Spesielt vil vi at du unngår å (prøve å):

* bryte ut av containeren eller kontakte andre tjenester/containere
* få tak i andre brukeres data eller tilganger, inkl. tilganger til andre systemer
* bruke opp maskinens ressurser
* gjøre tunge utregninger, f.eks. utvinning av kryptovaluta

(Om du kommer i skade for å gjøre noe sånt *likevel*, er det veldig greit om du
gir beskjed, så vi kan reparerere evt. skade og forhindre at det skjer igjen.)

Containeren stoppes / restartes automatisk hvis den bruker for mye ressurser
(f.eks. om du lager en uendelig løkke) eller hvis den ikke er i bruk på en
stund. Dette kan føre til korte avbrudd i tjenesten.

# Lagring av innhold

Vi bruker [Web Storage](https://en.wikipedia.org/wiki/Web_storage) i din egen
nettleser for å lagre sesjonen din og ting du programmerer / lager («innhold»).
Dette er data du kan slette selv i nettleseren, eventuelt kan nettleseren være
satt til å bare lagre data så lenge vinduet er åpent (f.eks. i «privat modus»).

Siden dette er en eksperimentell nettside lagrer vi for øyeblikket *ikke*
innholdet ditt hos oss, utover midlertidig lagring som er nødvendig for
å kompilere og kjøre programmer. Det betyr at du ikke får tilgang til dataene
fra en annen nettleser eller om du tømmer «Web Storage».
