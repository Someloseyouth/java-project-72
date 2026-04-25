@import hexlet.code.dto.UrlPage
@param UrlPage page

@template.layout(
    page = page,
    content = @`
        <h1>Сайт: ${page.getUrl().getName()}</h1>

        <table class="table table-bordered" data-test="url">
            <tbody>
                <tr>
                    <td>ID</td>
                    <td>${page.getUrl().getId()}</td>
                </tr>
                <tr>
                    <td>Имя</td>
                    <td>${page.getUrl().getName()}</td>
                </tr>
                <tr>
                    <td>Дата создания</td>
                    <td>${page.getUrl().getCreatedAt().toLocalDate().toString()}</td>
                </tr>
            </tbody>
        </table>

        <form method="post" action="/urls/1/checks">
                  <input type="submit" value="Запустить проверку">
        </form>

        <table data-test="checks">
          <thead>
            <tr>
              <th>ID</th>
              <th>Код ответа</th>
              <th>h1</th>
              <th>title</th>
              <th>description</th>
              <th>Дата создания</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>1</td>
              <td>200</td>
              <td>...</td>
              <td>...</td>
              <td>...</td>
              <td>2026-01-01</td>
            </tr>
          </tbody>
        </table>
    `
)