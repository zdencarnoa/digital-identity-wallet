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
- enkripcija PID-a korištenjem simetrične kriptografije (AES)
- sigurna pohrana, generiranje i upravljanje privatnim ključevima
- autentikacija putem verifiable presentation-a(VP) mehanizma
- biometrijska autentikacija korisnika

## Arhitektura sustava
Komponente sustava:
* Novčanik (wallet)
  * generiranje i pohrana privatnog ključa u Android Keystore-a
  * pristup pomoću PIN-a ili biometrije
  * lokalna pohrana PID-a
  * generiranje VP-a
* Izdavatelj (issuer)
  * izdaja PID vjerodajnica
  * povezivanje identiteta s javnim ključem korisnika
* Verifier
  * provjera VP-a i autentifikacija korisnika

## Kriptografija sustava
* Privatni ključ
  * generira se u Android Keystore sustavu
  * vezan uz PID
* Javni ključ
  * dio PID vjerodajnice
  * koristi se za verifikaciju
* MEK(Master Encryption Key)
  * koristi se za enkripciju PID-a
  * deriviran pomoću PIN-a
* PIN/biometrija
  * koriste se za pristup privatnom ključu
  * implementirani putem Android Keystore-a

PIN -> derivacija ključa -> derivacija MEK-a -> dekripcija i dohvat privatnog ključa 

## Tehnologije
* Android(Java)
  * Android Keystore
* Python - FastAPI
* Nginx, OpenSSL, AKDCA Root - uspostava mTLS-a i certifikati
* Certillia middleware - čitanje eOsobne putem preglednika