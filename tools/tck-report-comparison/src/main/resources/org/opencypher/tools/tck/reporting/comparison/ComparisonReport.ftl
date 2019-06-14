<!DOCTYPE html>
<html>
<head>
    <title>TCK Comparison Report</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <style>
        .padded {
            margin: 20px 0;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>TCK Comparison Report</h1>
    <#if diff.newlyFailedScenarios?size == 0 && diff.newlyPassingScenarios?size != 0>
        <div class="alert alert-success">
            <strong>Success!</strong> TCK coverage improved.
        </div>
    <#elseif diff.newlyFailedScenarios?size != 0>
        <div class="alert alert-danger">
            <strong>Warning!</strong> TCK coverage degraded.
        </div>
    <#else>
        <div class="alert alert-info">
            <p>Nothing changed.</p>
        </div>
    </#if>

    <#macro scenarios scenarios>
        <#if scenarios?size != 0>
            <table class="table table-striped padded">
                <tr>
                    <th>Feature</th>
                    <th>Scenario</th>
                    <th>Status</th>
                </tr>
                <#list scenarios as scenario>
                    <tr>
                        <td>${scenario.featureName}</td>
                        <td>${scenario.name}</td>
                        <td<#if scenario.status == 'Passed'> class="success"<#else> class="danger"</#if>>
                            ${scenario.status}
                        </td>
                    </tr>
                </#list>
            </table>
        </#if>
    </#macro>

    <#if diff.newlyFailedScenarios?size != 0>
        <h2>
            Newly failed scenarios: <span class="label label-danger">${diff.newlyFailedScenarios?size}</span>
        </h2>
        <@scenarios scenarios=diff.newlyFailedScenarios/>
    </#if>

    <#if diff.newlyPassingScenarios?size != 0>
        <h2>
            Newly passing scenarios: <span class="label label-success">${diff.newlyPassingScenarios?size}</span>
        </h2>
        <@scenarios scenarios=diff.newlyPassingScenarios/>
    </#if>

    <hr/>

    <h2>
        Coverage:
        <span class="label label-info">${diff.totalPassingScenarios}</span> scenarios out of
        <span class="label label-info">${diff.totalScenarios}</span> /
        <span class="label label-info">${diff.passingPercentage}</span>
    </h2>
    <@scenarios scenarios=diff.allScenarios/>
</div>
</body>
</html>
