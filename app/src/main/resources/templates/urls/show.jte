@import hexlet.code.util.NamedRoutes
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

        <form method="post" action="${NamedRoutes.urlCheckPath(page.getUrl().getId())}">
            <input type="submit" value="Запустить проверку">
        </form>

               <table class="table table-bordered" data-test="checks">
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
                       @for (var check : page.getChecks())
                           <tr>
                               <td>${check.getId()}</td>
                               <td>${check.getStatusCode()}</td>
                               <td>${check.getH1()}</td>
                               <td>${check.getTitle()}</td>
                               <td>${check.getDescription()}</td>
                               <td>${check.getCreatedAt().toLocalDate().toString()}</td>
                           </tr>
                       @endfor
                   </tbody>
               </table>
    `
)