@import hexlet.code.dto.UrlsPage
@import hexlet.code.util.NamedRoutes
@param UrlsPage page

@template.layout(
    page = page,
    content = @`
        <h1>Сайты</h1>

        <table class="table table-bordered" data-test="urls">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Имя</th>
                    <th>Последняя проверка</th>
                    <th>Код ответа</th>
                </tr>
            </thead>
            <tbody>
                @for (var url : page.getUrls())
                    <tr>
                        <td>${url.getId()}</td>
                        <td>
                            <a href="${NamedRoutes.urlPath(url.getId())}">${url.getName()}</a>
                        </td>
                        <td>
                            @if (url.getLastCheckAtFormatted() != null)
                                ${url.getLastCheckAtFormatted()}
                            @endif
                        </td>
                        <td>
                            @if (url.getLastStatusCode() != null)
                                ${url.getLastStatusCode()}
                            @endif
                        </td>
                    </tr>
                @endfor
            </tbody>
        </table>
    `
)