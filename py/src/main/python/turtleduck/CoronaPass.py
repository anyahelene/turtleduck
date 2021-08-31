#from asn1crypto import pem, x509
#import json
#for cc in 'AT BE BG CH CZ DE DK FI GR HR IS IT LI LT LV PL PT RO SE SI SK'.split():
#	with open(f'dcc/dcc-testdata/{cc}/2DCode/raw/1.json') as f:
#		testdata = json.load(f)
#		if 'CERTIFICATE' in testdata['TESTCTX']:
#			data = loads(decompress(b45decode(testdata['BASE45'])))
#			header = loads(data.value[0])
#			if 4 in header and header[1] == -7:
#				#print(cc, testdata['TESTCTX']['CERTIFICATE'])
#				cert = x509.Certificate.load(b64decode(testdata['TESTCTX']['CERTIFICATE']))
#				#print(header[1], cert.public_key['public_key'])
#				verify_key = VerifyingKey.from_string(cert.public_key['public_key'].native, ecdsa.NIST256p)
#				TRUSTED[header[4]] = verify_key.to_pem() + cc.encode()
                                
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

CWT_CLAIMS=[
        ('iss', 1, 'issued by'),
        ('exp', 4, 'expires'),
        ('iat', 6, 'issued at'),
        ('hcert', -260, 'hcert')
]

COSE_PARAMS=[
        ('alg', 1, 'algorithm'),
        ('kid', 4, 'key id')
]
COSE_ALGS=[
        ('ES256', -7, 'ECDSA, NIST256p, sha256'),
        ('PS256', -37, 'RSASSA-PSS, 2048 bits, sha256')
]

def getByName(db, name):
    for entry in db:
        if entry[0] == name:
            return entry
    return (name,0,None)

def getById(db, id):
    for entry in db:
        if entry[1] == id:
            return entry
    return ('<unknown>',id,None)

