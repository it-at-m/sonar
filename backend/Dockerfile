# For documentation see https://jboss-container-images.github.io/openjdk/
FROM registry.access.redhat.com/ubi10/openjdk-21-runtime:1.24-22@sha256:f6bbd27d73c4aacec336a450af2ebdcc5993d75438878550ea5250c4cd023457

# Copy runnable jar to deployments
COPY target/*.jar /deployments/application.jar