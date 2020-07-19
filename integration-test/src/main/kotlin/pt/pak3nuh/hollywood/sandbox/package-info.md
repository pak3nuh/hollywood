# Sandbox environment

This package is intended to test the framework in an E2E fashion, ensuring regression while the project
iterates into the final stages.

## Problem statement

We will model a very simple pet clinic. The pet owner can check in it's pet, being for a routine
appointment or to be interned.

For interactions with the system, a series of façades will be available, like

- Owner façade
  - Checkin a pet
  - Register for status updates
  - See the pet, which may be unavailable
  - Checkout the pet

- Vet façade
  - Perform analysis
  - Contact owner
  - Apply treatment

- Clinic façade
  - Check pets inside
  - Other reports

## Success metrics

It is the goal of the experiment to successfully create a set of deterministic scenarios that can
be used to check system compatibility.

It's expected that some tweaks may be needed to incorporate some new features like message serialization.
