# Maharashtra Property Registration Document Checklist

> This checklist is an educational planning aid for the project, not legal advice. Requirements vary by transaction type, property type, parties and the registering office. Confirm the final list with the official Maharashtra Department of Registration & Stamps before a real transaction.

## Core documents shown in the public guide

### Required for most registrations

- Original document to be registered, such as the signed sale deed or conveyance deed.
- Proof that applicable stamp duty and registration fee were paid.
- Photo identity cards for all parties who will appear to admit execution.
- Identity documents for witnesses or persons identifying the parties.
- Recent photographs where the registering office requires them.
- Property-specific supporting papers based on the document category.

### Property and title evidence

- Previous title deed or title chain.
- Property card or 7/12 extract where applicable.
- Latest mutation/revenue record where applicable.
- Latest municipal/property tax receipt.
- Encumbrance or charge-related certificate where applicable.
- Approved plan, commencement certificate or occupancy/completion certificate where applicable.
- Society or authority NOC where applicable.
- RERA or builder documents for applicable new/under-construction property.

### Special situations

- Registered Power of Attorney and prescribed declaration when someone represents a party.
- Legal-heir, succession, probate or death documents for inherited property.
- Loan, lender NOC or hypothecation papers when financing is involved.
- NRI/foreign-party documents and authorization where applicable.

### Registration-day references

- Public Data Entry number and preregistration summary if that service was used.
- e-Step appointment receipt if an appointment was booked.
- Document-handling fee/payment reference where applicable.

## Upload model implemented in the application

The current MVP asks the applicant for one supporting document at a time and supports:

- PDF
- JPG/JPEG
- PNG

For a realistic next release, replace the single-document screen with a checklist-driven upload area containing:

```text
Document category
Required or optional label
Why it is needed
Accepted file types
Maximum file size
Upload status
Applicant declaration
Officer review status
```

Recommended categories:

```text
Identity Proof
Address Proof
Signed Sale Deed / Conveyance Deed
Title / Previous Deed
Property Card or 7/12 Extract
Property Tax Receipt
Stamp Duty / Registration Fee Proof
Encumbrance / NOC / Approval (if applicable)
Special Authorization (if applicable)
```

## Important verification wording

The system should say **“document uploaded”**, not **“document legally verified”**, until an authorized officer has reviewed it. File extension checks alone cannot prove authenticity, ownership, title, stamp duty or legal validity.

## Official references

- Maharashtra Department of Registration & Stamps checklist: https://igrmaharashtra.gov.in/Home/checklist
- Official registration-day document list PDF: https://igrmaharashtra.gov.in/pdf/eodb/3.91List_of_Documents.pdf
- Government of Maharashtra registering-property process note: https://igrmaharashtra.gov.in/pdf/eodb/1.3Registering_Property_Mumbai_EoDB.pdf

Last reviewed for this project: 16 September 2026.
