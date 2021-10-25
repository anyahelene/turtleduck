# CBOR Web Token claims (i.e., fields in the data structure)
CWT_ISS=1
CWT_EXP=4
CWT_IAT=6
CWT_HCERT=1

CWT_CLAIMS={
        1: 'iss', # issued by
        4: 'exp', # expires (timestamp)
        6: 'iat', # issued at (timestamp)
        -260: 'hcert' # EU health certificate
}

# COSE signature header
COSE_ALG=1
COSE_KID=4
COSE_PARAMS={
        1: 'alg',  # signature algorithm
        2: 'crit', # critical headers – list of fields you *must* understand to use the token
        3: 'content type', # Content type of the payload
        4: 'kid',  # signing key id
        5: 'IV',   # Initialization vector
        6: 'Partial IV',  # Partial initialization vector
        7: 'counter signature', # Counter signature
        10: 'kid context',  # Identifies the context for the key identifier
}
COSE_ALG_ES256=-7
COSE_ALG_PS256=-37

COSE_ALGS={
        -7: 'ES256', # ECDSA, NIST256p, sha256'
        -37: 'PS256', # RSASSA-PSS, 2048 bits, sha256
}

def load_certificate_from_testdata(testdata):
    from asn1crypto import pem, x509
    if 'CERTIFICATE' in testdata['TESTCTX']:
        data = loads(decompress(b45decode(testdata['BASE45'])))
        header = loads(data.value[0])
        if COSE_KID in header and header[COSE_ALG] == COSE_ALG_ES256:
            #print(cc, testdata['TESTCTX']['CERTIFICATE'])
            cert = x509.Certificate.load(b64decode(testdata['TESTCTX']['CERTIFICATE']))
            #print(header[1], cert.public_key['public_key'])
            verify_key = VerifyingKey.from_string(cert.public_key['public_key'].native, ecdsa.NIST256p)
            TRUSTED[header[COSE_KID]] = verify_key.to_pem() + cc.encode()

def load_all_certificates_from_testdata():
    import json
    for cc in 'AT BE BG CH CZ DE DK FI GR HR IS IT LI LT LV PL PT RO SE SI SK'.split():
    	with open(f'dcc/dcc-testdata/{cc}/2DCode/raw/1.json') as f:
            load_certificate_from_testdata(json.load(f))

EXAMPLES={
        'SE-1': 'HC1:NCFOXN%TSMAHN-HVN8J7UQMJ4/3RZLH62V2G1PC9CMSRH+QKFNTAVD3B19*AJCBMF6.UCOMIN6R%E5BD7HG8CU6O8QGU68ORJSPAEQOIR+SPCVO.28DDQHQ1BW9XX7ZY7NTICZU1*8X/KQ96/-KKTCY73JC3KD3LWT HB3ZC64JX7JQ1LK$2965VMFD-48YI 3533LC4TZ0BR/S09T./0ZYTS P-$0R:67PPDFPVX1R270:6C$Q0R6EOMUF5LDCPF5RBQ746B46O1N646RM9AL5CBVW566LH 469/9-3AKI6%T6LEQ-P6UQK*%NH$RSC9FFFW+7H9N$W2JO2C6S3UJ92KEST.ZJ-8B ZJ83B 2TAAUZZ2LH2%EUBUJZ0KZPIR145%T0YIF0JEYI1DLNCK1627ACW-T%NSY18KT911GL.EHNTI+SB-5A-ARUQNFW$ 2:.NU6W/CU8WDTFVG:BG3JFCSAVH-4V:HP4$0/.D9OV-RM60R7Z3B8PXICK+L/S1P*O:FG'
}

TRUSTED={
        b'\x19\x9d\x9c|\xc2\xf0\xc8\xaf':
        b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEFzWPFb4pCXJONcqkz+MsHoHCrEw7\nFTFpRwDj7w380LPp9U//ddpWvUkMOK888mB6qAviPllcMJJFXAzoo2+gfg==\n-----END PUBLIC KEY-----\nSE', 
        b'\xd9\x197_\xc1\xe7\xb6\xb2': 
        b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAErdVc9a0bltR6jm1BPTA3u0cyJNYK\nuF1uRk8h7h04+XBRJ9kYHt+/oSDXwmWXKM6cECncmqaKz1D9UxO1FpdBdw==\n-----END PUBLIC KEY-----\nAT', 
        b'\x94q\xd1\x84\xca=\x19h': 
        b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAES0WaB+wj6gZtAxyarewP7DwNVFxq\nGzcR/nHGkxWdfSQ7SsKXXn008p+2rZiuem4qQkN/muxX1pbu/brFstaNgQ==\n-----END PUBLIC KEY-----\nBE', 
        b'\xb8N\xd5\x89\x84\xd2z\x08': 
        b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEvQkNQqranGNTlHFjWXxt0alM1Em0\nByl0K3414K+Lh4SFM/Psoh12oGobhNvKUEypMNgIeuxPE4/iIFGtATenJQ==\n-----END PUBLIC KEY-----\nDK', 
       b'\xbb\x1b\xe5\xf9\xdb2\xac\x1c': 
       b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEBcc6ApRZrh9/qCuMnxIRpUujI19b\nKkG+agj/6rPOiX8VyzfWvhptzV0149AFRWdSoF/NVuQyFcrBoNBqL9zCAg==\n-----END PUBLIC KEY-----\nGR', 
       b'\xcbx6\xee\xff+\xe7R': 
       b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/A2niKWybkqciyvlA8qltQjWbYsi\neJ3ryOqBXqFukUVzD24I1YGdhcp+OgUMPDKdSOiX0fqhMPuK51xnH6xWlg==\n-----END PUBLIC KEY-----\nHR', 
       b'5&\xeb\xa1\x16J\x1dT': 
       b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEr/Of2kK3BRGoz6XKyKahGHjjZsK8\nQQVomXkUUAGwBDnF/B0NSTzd8TdFiVqIFPXpILDbuFdPyq5PwYL3iSaZMA==\n-----END PUBLIC KEY-----\nIS', 
       b'90\x17h\xcd\xda\x05\x13': 
       b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENKnu3zonECaNkWhuaa7TCSKLneyd\nG7CpVZ7f+D/BXP2LfTpcDmJekeRE9lVSRJmZiG60wOuF6Dqx/AxF2qXdpg==\n-----END PUBLIC KEY-----\nIT', 
       b't\x06\x9c A\x8c5\xc1': 
       b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEdRONZMFNTpyg8cRP8uVmscHjdfKo\ntSCTIfnZQb9NWuBPyZhts9ChT08WywqmqIQ1Z3uxmXgQzAf2FRWSv9tzIQ==\n-----END PUBLIC KEY-----\nLI', 
       b'\x08U(:\x15m:\n': 
       b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEFvlXRFujlBzXIBXFHJdou39COYjh\nYbeqxd/zVagfovZR/iiPmIacC56bVNKi1rH4VSC+f4ZtxSDsFOsv548BBA==\n-----END PUBLIC KEY-----\nPL', 
       b'\x82IV\xa6\xabV\xf98': 
       b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE0t4JasNP49m0muhS27VfC1dq2ntZ\nbGcXsIDnRNAJSGiYsJDeNdFsTgP7R/N2nbJZpMd+IP+TW/g5UBfnPrTv6A==\n-----END PUBLIC KEY-----\nPT', 
       b'\x9b\xab(\xd0\x8d\xae#,': 
       b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEKr13WPR4mLTAxbD8l8bk4fFPn5lM\n4L2mk6TEeFYe2CS9R9al6j1YfAZM4FFgl8KHCVur2s8zWUrAQcA97lQE1A==\n-----END PUBLIC KEY-----\nRO', 
       b'\x82\xdb\x10M\x1f6W\xac': 
       b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEp7NEJHAyIVwZPfB5bvz4756ICGwh\nOn5iRhWJHuREQSvtGpQfVK20PgMTTgUCmYQBkSw5mlKgpRNcn2RVnGEAHg==\n-----END PUBLIC KEY-----\nSI', 
       b'\xa6\x038t;@\x05(': 
       b'-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEW6fufxhi1gBb42JIEHwd+f4dhypR\n0w8kDHgW/x+JZgN52Lr0QgT11KLczucc38tTIGlPA/F45bmSivAfWeJUHg==\n-----END PUBLIC KEY-----\nSK'
}


#data = cbor2.loads(cose_data[2])
#data[-260][1]['v'][0]['is'] = 'Puffling eHealth Authority'
#data[1] = 'PF'
#data[-260][1]['nam'] = {'fn': 'Duck', 'gn': 'Turtle', 'fnt' : 'DUCK', 'gnt' : 'TURTLE'}
#data[-260][1]['dob'] = '2021-02-03'
#data[-260][1]['v'][0]['co'] = 'PF'
#newcert = cose_data.copy()
#newcert[2] = cbor2.dumps(data)
#b'HC1:' + b45encode(zlib.compress(cbor2.dumps(cbor2.CBORTag(18, newcert))))
