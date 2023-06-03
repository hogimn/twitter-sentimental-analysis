<#macro noauthentication title="Welcome">
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name=viewport content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="/style/reset.css">
        <link rel="stylesheet" href="/style/style.css">
        <link rel="icon" type="image/svg" href="/favicon.svg">
        <title>Twitter Sentimental Analysis</title>
    </head>
    <body>
    <header>
        <div class="container">
            <h1>Twitter Sentimental Analysis</h1>
        </div>
    </header>
    <section class="callout">
        <div class="container">
            an <span class="branded">AppContinuum[]</span> application with background workers.
        </div>
    </section>
    <main>
        <#nested>
    </main>
    <footer>
        <div class="container">
            <script>document.write("©" + new Date().getFullYear());</script>
            Initial Capacity, Inc. All rights reserved.
        </div>
    </footer>
    </body>
    </html>
</#macro>