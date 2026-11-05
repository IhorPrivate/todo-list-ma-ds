// Runs once, on an empty data directory, as the root user.
// Each service owns its own database; neither can read the other's.
const services = [
  { database: 'tasks', user: process.env.ADDTASK_DB_USER, password: process.env.ADDTASK_DB_PASSWORD },
  { database: 'notifications', user: process.env.NOTIFICATION_DB_USER, password: process.env.NOTIFICATION_DB_PASSWORD },
];

for (const service of services) {
  if (!service.user || !service.password) {
    throw new Error(`Missing credentials for database ${service.database}`);
  }
  db.getSiblingDB(service.database).createUser({
    user: service.user,
    pwd: service.password,
    roles: [{ role: 'readWrite', db: service.database }],
  });
  print(`Created user ${service.user} for database ${service.database}`);
}
