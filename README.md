# Digital-identity-wallet
Ovaj repozitorij sadrži implementaciju inačice EUDI novčanika u sklopu završnog rada na Fakultetu elektrotehnike i računarstva.
Projekt obuhvaća implementaciju nekih od funkcionalnosti definiranih službenim EUDI-wallet projektom. 

## Opis projekta
Projekt obuhvaća sljedeće ključne funkcionalnosti:
1. Dodjela i sigurna pohrana osobnog identifikatora(PID-a)
2. Dodjela i mehanizam upravljanja kriptografskim ključevima
3. Autentifikacija korištenjem verifiable presentation(VP) mehanizma

Arhitektura sustava podijeljena je na: 
* Android aplikaciju(novčanik)
  * upravljanje ključevima i autentikacija
* Python backend
  * izdaja vjerodajnica i provjera identiteta

## Funkcionalnosti
- dodjeljivanje osobnog identifikatora(PID-a) putem e-Osobne
- sigurna pohrana, generiranje i upravljanje privatnim ključevima u Android Keystore sustavu
- ugrađivanje javnog ključa korisnika u PID prilikom izdavanja (key binding)
- individualni potpis atributa PID-a radi mogućnosti selektivnog otkrivanja podataka
- autentikacija pomoću prezentacije PID-a uz dokaz posjeda privatnog ključa (potpisivanje nonce-a od strane verifiera)
- biometrijska autentikacija korisnika

## Arhitektura sustava
Komponente sustava:
* Novčanik (wallet)
  * generiranje i pohrana privatnog ključa u Android Keystore
  * pristup pomoću PIN-a ili biometrije
  * lokalna pohrana PID-a
  * generiranje prezentacije i potpisivanje nonce-a pri autentikaciji
* Izdavatelj (issuer)
  * izdaja PID vjerodajnica
  * povezivanje identiteta s javnim ključem korisnika
  * individualno hashiranje atributa uz sol i potpisivanje liste hasheva
* Verifier
  * provjera potpisa issuer-a i dokaza posjeda privatnog ključa
  * autentifikacija korisnika

## Kriptografija sustava
* Privatni ključ
  * generira se i trajno ostaje u Android Keystore sustavu
  * korštenje vezano uz PIN ili biometriju
* Javni ključ
  * ugrađen u PID vjerodajnicu prilikom izdavanja
  * koristi se za provjeru dokaza posjeda privatnog ključa
* Atributi PID-a
  * svaki atribut hashira se zasebno uz salt
  * issuer potpisuje listu hasheva atributa - selektivno otkrivanje pojedinih atributa
* PIN/biometrija
  * koriste se za pristup privatnom ključu
  * implementirani putem Android Keystore-a

Tok autentikacije korisnika: PIN ili biometrija -> Android Keystore otključava korištenje privatnog ključa -> wallet potpisuje nonce verifiera -> verifier provjerava potpis javnim ključem iz PID-a

## Tehnologije
* Android(Java)
  * Android Keystore
* Python - FastAPI
* Nginx, OpenSSL, AKDCA Root - uspostava mTLS-a i certifikati
* Certillia middleware - čitanje eOsobne putem preglednika

## Ograničenja implementacije 
* implementacija ne koristi službeni EUDI format vjerodajnica(SD-JWT VC) niti protokole OpenID4VCI i OpenID4VP
* struktura PID-a i kriptografski model napravljeni su uz mogućnost nadogradnje na službene EUDI formate i protokole bez redizajna temeljnog dijela sustava