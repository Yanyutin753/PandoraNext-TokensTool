from pandora.openai.auth import Auth0
def share_token_config():
    try:
        username = 'it1oespb@protonmail.com'
        password = 'ynwgjxdk'
        proxy = ''
        print('Login begin: {}'.format(username))
        try:
            token = Auth0(username, password, proxy).auth(False)
            print('Login success: {}'.format(username))
            print(token)
        except Exception as e:
            err_str = str(e).replace('\n', '').replace('\r', '').strip()
            print('Login failed: {}, {}'.format(username, err_str))
            token= err_str
        return token
    except Exception as e:
        print(f"An error occurred: {str(e)}")
        return None

