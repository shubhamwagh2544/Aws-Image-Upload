# Aws-Image-Upload

- Created App User and two roles USER, ADMIN.
- Created Registration controller which takes registration request and signs up the app user and generates the UUID token at same time and sends it to the user email using java mail sender service on fake Maildev server listening on customized port in application.yml configuration file.
- User has to confirm the token by clicking 'Activate Account' link in received email and token is confirmed -
- Thus App user is authenticated and User can login now.
