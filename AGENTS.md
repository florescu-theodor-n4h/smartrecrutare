# Ghid pentru agenti

## Reguli de proiect

- Foloseste Java 17+ si Maven; nu introduce dependinte fara o justificare clara.
- Pastreaza separarea controller -> serviciu -> mapper -> repository.
- Scrie comentarii/Javadoc in romana pentru logica de domeniu sau autorizare;
  evita comentariile care doar repeta codul.
- Valideaza toate request-urile cu Bean Validation si pastreaza raspunsurile
  `ProblemDetail` gestionate de `HandlerExceptiiGlobal`.
- Pastreaza regulile RBAC. Pentru orice actiune asupra unui angajator sau job,
  managerul local trebuie autorizat prin `LocalAuthorizationService`.

## Testare si siguranta

- Adauga teste unitare pentru fiecare comportament nou si teste de integrare
  cand se modifica ruta, autentificarea sau autorizarea.
- Ruleaza `mvn test` si `mvn package` dupa modificarile de cod.
- **Nu rula niciodata `gh auth refresh` si nu reinnoi autentificarea GitHub.**
  Foloseste sesiunea deja autentificata; daca lipseste un scop necesar, opreste
  operatia si raporteaza limitarea fara a solicita sau initia reautorizarea.
- Nu adauga secrete, parole, chei private, tokenuri, fisiere `.env` sau copii
  ale configuratiilor locale.
- Nu modifica sau activa profilurile de deployment, configuratiile AWS ori
  credentialele infrastructurii pentru schimbari de functionalitate locala.
